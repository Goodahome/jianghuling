# QA section 9.6 + 9.7 - ASCII only
$ErrorActionPreference='Continue'
$base='http://localhost:8080/api/v1'
$outDir='f:\Jinanghu_Ling\docs\_qa_run'
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$R=[ordered]@{}

function Invoke-Api($method,$path,$jsonBody=$null,$token=$null) {
  $headers=@{}
  if ($token) { $headers['Authorization']="Bearer $token" }
  $uri="$base$path"
  try {
    if ($null -ne $jsonBody) {
      $bytes=[System.Text.Encoding]::UTF8.GetBytes($jsonBody)
      $resp=Invoke-WebRequest -Method $method -Uri $uri -Headers $headers -ContentType 'application/json; charset=utf-8' -Body $bytes -UseBasicParsing
    } else {
      $resp=Invoke-WebRequest -Method $method -Uri $uri -Headers $headers -UseBasicParsing
    }
    return ($resp.Content | ConvertFrom-Json)
  } catch {
    if ($_.Exception.Response) {
      $sr=New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream(), [Text.Encoding]::UTF8)
      $txt=$sr.ReadToEnd()
      try { return ($txt | ConvertFrom-Json) } catch { return @{ code=-1; message=$txt } }
    }
    return @{ code=-1; message=$_.Exception.Message }
  }
}
function Pass($id,$ok,$detail){ $R[$id]=@{pass=[bool]$ok; detail=$detail} }
function Invite-Code($token) {
  $inv=Invoke-Api POST '/admin/invites' '{"quota":5,"remark":"v18"}' $token
  if ($inv.data.codes) { return @($inv.data.codes)[0] }
  return $inv.data.code
}

$adm=Invoke-Api POST '/admin/auth/login' '{"username":"admin","password":"admin123"}'
$at=$adm.data.token
Pass 'ADM' ($adm.code -eq 0) @{code=$adm.code}

$suffix=Get-Random -Min 10000 -Max 99999
$c1=Invite-Code $at; $c2=Invite-Code $at
$phoneP=('137{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$phoneC=('138{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$userP="v18p$suffix"; $userC="v18c$suffix"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneP`",`"scene`":`"REGISTER`"}" | Out-Null
$regP=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$c1`",`"phone`":`"$phoneP`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userP`",`"nickname`":`"P$suffix`"}"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneC`",`"scene`":`"REGISTER`"}" | Out-Null
$regC=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$c2`",`"phone`":`"$phoneC`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userC`",`"nickname`":`"C$suffix`"}"
$tokenP=$regP.data.token; $tokenC=$regC.data.token
$idP=$regP.data.user.id; $idC=$regC.data.user.id
$bal0=[decimal](Invoke-Api GET '/wallet/account' $null $tokenP).data.balance
Pass 'REG' (($regP.code -eq 0) -and ($regC.code -eq 0) -and ($bal0 -ge 500)) @{ P=$regP.code; C=$regC.code; idP=$idP; idC=$idC; bal0=$bal0 }

$deadline=(Get-Date).ToUniversalTime().AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$bjson='{"type":"RENT_SEEK","title":"V18-SRC-'+$suffix+'","difficulty":"EASY","rewardAmount":200,"confirmLowReward":false,"deadlineAt":"'+$deadline+'","taskTags":["t"],"checklistItemCodes":["VERIFY_AUTHENTIC","SITE_VISIT_RECORD"],"warrantFields":{"district":"H","rentBudgetMin":800,"rentBudgetMax":1000,"layout":"2","expectMoveInDate":"2026-09-01","acceptAgency":false,"extra":""}}'
$create=Invoke-Api POST '/bounties' $bjson $tokenP
$srcId=$create.data.id
Pass 'CREATE' (($create.code -eq 0) -and ($create.data.status -eq 'PENDING_REVIEW')) @{ id=$srcId; status=$create.data.status }

$denyOpen=Invoke-Api POST ("/bounties/"+$srcId+"/republish") '{}' $tokenP
Pass 'V18-06-DENY-PENDING' ($denyOpen.code -eq 43007) @{ code=$denyOpen.code; msg=$denyOpen.message }

$rej=Invoke-Api POST ("/admin/bounty-reviews/"+$srcId) '{"result":"REJECT","reason":"qa reject for republish"}' $at
$srcDetail=Invoke-Api GET ("/bounties/"+$srcId) $null $tokenP
$accAfterRej=Invoke-Api GET '/wallet/account' $null $tokenP
Pass 'REJECT' (($rej.code -eq 0) -and ($srcDetail.data.status -eq 'REJECTED') -and ($srcDetail.data.canRepublish -eq $true)) @{
  rej=$rej.code; status=$srcDetail.data.status; canRepublish=$srcDetail.data.canRepublish; bal=$accAfterRej.data.balance; frozen=$accAfterRej.data.frozen
}

$denyOther=Invoke-Api POST ("/bounties/"+$srcId+"/republish") '{}' $tokenC
Pass 'V18-06-DENY-NONOWNER' (($denyOther.code -eq 43007) -or ($denyOther.code -eq 40300)) @{ code=$denyOther.code; msg=$denyOther.message }

$draft=Invoke-Api GET ("/bounties/"+$srcId+"/republish-draft") $null $tokenP
Pass 'V18-DRAFT' (($draft.code -eq 0) -and ($draft.data.sourceBountyId -eq $srcId)) @{
  code=$draft.code; sourceBountyId=$draft.data.sourceBountyId; title=$draft.data.title; reward=$draft.data.rewardAmount
}

$balBefore=[decimal]$accAfterRej.data.balance
$newDeadline=(Get-Date).ToUniversalTime().AddDays(10).ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$repBody='{"deadlineAt":"'+$newDeadline+'","confirmLowReward":true}'
$rep=Invoke-Api POST ("/bounties/"+$srcId+"/republish") $repBody $tokenP
Pass 'V18-02-DIAG' ($rep.code -eq 0) @{ code=$rep.code; msg=$rep.message; id=$rep.data.id; data=$rep.data }
$newId=$rep.data.id
$newDetail=Invoke-Api GET ("/bounties/"+$newId) $null $tokenP
$srcAfter=Invoke-Api GET ("/bounties/"+$srcId) $null $tokenP
$accAfterRep=Invoke-Api GET '/wallet/account' $null $tokenP
$mine=Invoke-Api GET '/bounties/mine/published?page=1&pageSize=20' $null $tokenP
$mineNew=@($mine.data.list | Where-Object { $_.id -eq $newId } | Select-Object -First 1)

Pass 'V18-02-NEW-ID' (($rep.code -eq 0) -and ($newId -ne $srcId) -and ($rep.data.sourceBountyId -eq $srcId)) @{
  newId=$newId; srcId=$srcId; sourceBountyId=$rep.data.sourceBountyId; status=$rep.data.status
}
Pass 'V18-03-PENDING-FREEZE' (($newDetail.data.status -eq 'PENDING_REVIEW') -and ([decimal]$accAfterRep.data.frozen -ge 200) -and ([decimal]$accAfterRep.data.balance -eq ($balBefore - 200))) @{
  status=$newDetail.data.status; frozenBal=$accAfterRep.data.frozen; bal=$accAfterRep.data.balance; balBefore=$balBefore
}
Pass 'V18-02-SRC-UNCHANGED' ($srcAfter.data.status -eq 'REJECTED') @{ status=$srcAfter.data.status }
Pass 'V18-05-SOURCE-FIELD' (($newDetail.data.sourceBountyId -eq $srcId) -and ($mineNew.sourceBountyId -eq $srcId)) @{
  detailSrc=$newDetail.data.sourceBountyId; listSrc=$mineNew.sourceBountyId
}

$detailVue=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\BountyDetailView.vue')
$mineVue=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\MyBountiesView.vue')
$pubVue=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\PublishBountyView.vue')
$feEntry=(($detailVue -match 'canRepublish') -and ($detailVue -match 'republishFrom') -and ($mineVue -match 'canRepublish') -and ($mineVue -match 'republishFrom') -and ($pubVue -match 'republishBounty') -and ($pubVue -match 'getRepublishDraft'))
Pass 'V18-01-FE-ENTRY' $feEntry @{ ok=$feEntry }

$ap=Invoke-Api POST ("/admin/bounty-reviews/"+$newId) '{"result":"APPROVE","reason":"ok"}' $at
$st1=(Invoke-Api GET ("/bounties/"+$newId)).data.status
$claim=Invoke-Api POST ("/bounties/"+$newId+"/claims") '{}' $tokenC
$d=Invoke-Api GET ("/bounties/"+$newId) $null $tokenC
$clist=@($d.data.checklist)
$parts=New-Object System.Collections.Generic.List[string]
foreach($c in $clist){ $parts.Add(('{"itemCode":"'+$c.itemCode+'","done":true,"text":"d","mediaUrls":[]}')) }
if($parts.Count -eq 0){
  $parts.Add('{"itemCode":"VERIFY_AUTHENTIC","done":true,"text":"d","mediaUrls":[]}')
  $parts.Add('{"itemCode":"SITE_VISIT_RECORD","done":true,"text":"d","mediaUrls":[]}')
}
$itemJson='['+($parts -join ',')+']'
$sub=Invoke-Api POST ("/bounties/"+$newId+"/submissions") ('{"summary":"d","items":'+$itemJson+'}') $tokenC
$sid=$sub.data.id; if(-not $sid){$sid=$sub.data.submissionId}
$sr=Invoke-Api POST ("/admin/submission-reviews/"+$sid) '{"result":"APPROVE","reason":"ok"}' $at
$prev=Invoke-Api GET ("/bounties/"+$newId+"/settlement/preview") $null $tokenP
$dist=$prev.data.distributable
$settle=Invoke-Api POST ("/bounties/"+$newId+"/settlement") ('{"items":[{"userId":'+$idC+',"amount":'+$dist+',"chivalryBonus":0}]}') $tokenP
$final=(Invoke-Api GET ("/bounties/"+$newId)).data.status
Pass 'REGRESS-LOOP' (($ap.code -eq 0) -and ($st1 -eq 'OPEN') -and ($claim.code -eq 0) -and ($sub.code -eq 0) -and ($sr.code -eq 0) -and ($settle.code -eq 0) -and ($final -eq 'COMPLETED') -and ([decimal]$prev.data.fee -eq 20)) @{
  ap=$ap.code; open=$st1; claim=$claim.code; sub=$sub.code; sr=$sr.code; settle=$settle.code; final=$final; fee=$prev.data.fee; dist=$dist
}

$doneDetail=Invoke-Api GET ("/bounties/"+$newId) $null $tokenP
Pass 'V18-COMPLETED-CAN' (($doneDetail.data.canRepublish -eq $true) -and ($doneDetail.data.status -eq 'COMPLETED')) @{
  can=$doneDetail.data.canRepublish; status=$doneDetail.data.status
}

# section 9.7 static
$hallLayout=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\layouts\HallLayout.vue')
$brv=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hall\BountyReviewView.vue')
$brd=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hall\BountyReviewDetailView.vue')
$srv=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hall\SubmissionReviewView.vue')
$srd=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hall\SubmissionReviewDetailView.vue')
$backBar=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\components\HallBackBar.vue')
$mineList=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\MyBountiesView.vue')

$visual=(($hallLayout -match 'brand-title') -and ($hallLayout -match 'jh-container') -and ($brv -match 'jh-section') -and ($brv -match 'jh-panel') -and ($brv -notmatch 'el-table'))
$listLikeMine=(($brv -match 'item jh-panel') -and ($mineList -match 'jh-panel') -and ($brv -match 'goDetail') -and ($srv -match 'goDetail'))
$clickDetail=(($brv -match '/hall/bounty-reviews/') -and ($brd -match 'HallBackBar') -and ($srv -match '/hall/submission-reviews/'))
$backOk=(($backBar -match 'HallBackBar' -or $backBar -match 'goBack') -and ($backBar -match 'router.push') -and ($brd -match 'HallBackBar') -and ($srd -match 'HallBackBar') -and ($brd -match '/hall/bounty-reviews') -and ($srd -match 'submission-reviews'))
$rejectReason=(($brv -match 'REJECT') -and ($brv -match 'prompt') -and ($brd -match 'prompt') -and ($brd -match 'REJECT'))

Pass 'V181-01-VISUAL' $visual @{ visual=$visual }
Pass 'V181-02-LIST-SHAPE' $listLikeMine @{ ok=$listLikeMine }
Pass 'V181-03-CLICK-DETAIL' $clickDetail @{ ok=$clickDetail }
Pass 'V181-04-BACK' $backOk @{ ok=$backOk }
Pass 'V181-05-REVIEW' $rejectReason @{ ok=$rejectReason }

$R['meta']=@{srcId=$srcId; newId=$newId; idP=$idP; idC=$idC; suffix=$suffix; ts=(Get-Date).ToString('s')}
($R|ConvertTo-Json -Depth 8)|Set-Content "$outDir\v18_results.json" -Encoding UTF8
$lines=@(); foreach($k in $R.Keys){ if($k -eq 'meta'){continue}; $lines += ("{0}={1}" -f $k,$(if($R[$k].pass){'PASS'}else{'FAIL'})) }
$lines|Set-Content "$outDir\v18_summary.txt"
Write-Host 'V18 DONE'
$lines
