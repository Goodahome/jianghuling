# QA section 11 E2E - ASCII only script
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
    $ex=$_.Exception
    if ($ex.Response) {
      $sr=New-Object System.IO.StreamReader($ex.Response.GetResponseStream(), [System.Text.Encoding]::UTF8)
      $txt=$sr.ReadToEnd()
      try { return ($txt | ConvertFrom-Json) } catch { return @{ code=-1; message=$txt } }
    }
    return @{ code=-1; message=$ex.Message }
  }
}

function Pass($id,$ok,$detail) {
  $R[$id]=@{ pass=[bool]$ok; detail=$detail }
}

# AC-W1 / W5
$wc=New-Object System.Net.WebClient
$wc.Encoding=[System.Text.Encoding]::UTF8
$raw=$wc.DownloadString("$base/meta/warrant-templates")
[System.IO.File]::WriteAllText("$outDir\warrant-templates.raw.json",$raw,[System.Text.Encoding]::UTF8)
$wt=$raw | ConvertFrom-Json
$seek=@($wt.data | Where-Object { $_.type -eq 'RENT_SEEK' })[0]
$labs=@{}
foreach($f in $seek.fields){ $labs[$f.key]=$f.label }
$buChong = -join ([char[]](0x8865,0x5145,0x8BF4,0x660E))
$extraOk = ($labs['extra'] -eq $buChong) -or ($raw.Contains($buChong))
Pass 'AC-W1' ($wt.code -eq 0 -and $labs.ContainsKey('district') -and $labs.ContainsKey('extra') -and $extraOk) @{ extraOk=$extraOk; keys=@($labs.Keys); extraLabel=$labs['extra'] }
Pass 'AC-W5' ($extraOk -and ($raw -notmatch 'otherRequirements') -and ($raw -notmatch '"key"\s*:\s*"remark"')) @{ extraOk=$extraOk }

$suffix=Get-Random -Minimum 10000 -Maximum 99999
$phoneA=('138{0:D8}' -f (Get-Random -Minimum 0 -Maximum 99999999))
$phoneB=('139{0:D8}' -f (Get-Random -Minimum 0 -Maximum 99999999))
$userA="qaa$suffix"; $userB="qab$suffix"

# AC-01
$noInv=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"INVALIDXX`",`"phone`":`"$phoneA`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userA`",`"nickname`":`"QA_A`"}"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneA`",`"scene`":`"REGISTER`"}" | Out-Null
$regA=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"JHOPEN1`",`"phone`":`"$phoneA`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userA`",`"nickname`":`"QAA$suffix`"}"
$tokenA=$regA.data.token
$idA=$regA.data.user.id
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneB`",`"scene`":`"REGISTER`"}" | Out-Null
$regB=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"JHOPEN2`",`"phone`":`"$phoneB`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userB`",`"nickname`":`"QAB$suffix`"}"
if ($regB.code -ne 0) {
  $adm0=Invoke-Api POST '/admin/auth/login' '{"username":"admin","password":"admin123"}'
  $at0=$adm0.data.token
  $inv=Invoke-Api POST '/admin/invites' '{"quota":5,"remark":"qa-e2e"}' $at0
  $icode=$inv.data.code
  if (-not $icode) { $icode=$inv.data.inviteCode }
  Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneB`",`"scene`":`"REGISTER`"}" | Out-Null
  $regB=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$icode`",`"phone`":`"$phoneB`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userB`",`"nickname`":`"QAB$suffix`"}"
}
$tokenB=$regB.data.token
$idB=$regB.data.user.id
Pass 'AC-01' (($regA.code -eq 0) -and ($noInv.code -ne 0) -and ($regB.code -eq 0)) @{ A=$regA.code; B=$regB.code; noInv=$noInv.code; idA=$idA; idB=$idB; aMsg=$regA.message; bMsg=$regB.message; phoneA=$phoneA; phoneB=$phoneB }

$loginA=Invoke-Api POST '/auth/login' "{`"loginType`":`"PASSWORD`",`"username`":`"$userA`",`"password`":`"Test1234`"}"
$tokenA=$loginA.data.token
Pass 'AC-02' ($loginA.code -eq 0 -and $tokenA) @{ code=$loginA.code }

$biz="qa-e2e-$suffix"
$r1=Invoke-Api POST '/wallet/recharge' "{`"amount`":1000,`"clientRequestId`":`"$biz`"}" $tokenA
$r2=Invoke-Api POST '/wallet/recharge' "{`"amount`":1000,`"clientRequestId`":`"$biz`"}" $tokenA
$acc=Invoke-Api GET '/wallet/account' $null $tokenA
Pass 'AC-03' ($r1.code -eq 0 -and $r2.code -eq 0 -and [decimal]$acc.data.balance -eq 1000) @{ bal=$acc.data.balance; r1=$r1.code; r2=$r2.code }

$deadline=(Get-Date).ToUniversalTime().AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$lowBody=@"
{"type":"RENT_SEEK","title":"low","difficulty":"EASY","rewardAmount":100,"confirmLowReward":true,"deadlineAt":"$deadline","taskTags":["tag"],"checklistItemCodes":["VERIFY_AUTHENTIC","SITE_VISIT_RECORD"],"warrantFields":{"district":"A","rentBudgetMin":800,"rentBudgetMax":1200,"layout":"2r","expectMoveInDate":"2026-09-01","acceptAgency":false,"extra":""}}
"@
$missBody=@"
{"type":"RENT_SEEK","title":"miss","difficulty":"EASY","rewardAmount":200,"confirmLowReward":false,"deadlineAt":"$deadline","taskTags":["tag"],"checklistItemCodes":["VERIFY_AUTHENTIC"],"warrantFields":{"district":"A"}}
"@
$low=Invoke-Api POST '/bounties' $lowBody $tokenA
$miss=Invoke-Api POST '/bounties' $missBody $tokenA
Pass 'AC-05' ($low.code -eq 43001 -and $miss.code -eq 43002) @{ low=$low.code; miss=$miss.code; missMsg=$miss.message }

$createBody=@"
{"type":"RENT_SEEK","title":"QA-E2E-$suffix","difficulty":"EASY","rewardAmount":200,"confirmLowReward":false,"deadlineAt":"$deadline","taskTags":["tag"],"checklistItemCodes":["VERIFY_AUTHENTIC","SITE_VISIT_RECORD"],"warrantFields":{"district":"MedSchool","rentBudgetMin":800,"rentBudgetMax":1000,"layout":"2BR","expectMoveInDate":"30d","acceptAgency":false,"extra":""}}
"@
$create=Invoke-Api POST '/bounties' $createBody $tokenA
$bountyId=$create.data.id
$acc2=Invoke-Api GET '/wallet/account' $null $tokenA
Pass 'AC-04' ($create.code -eq 0 -and $create.data.status -eq 'PENDING_REVIEW' -and [decimal]$acc2.data.frozen -ge 200) @{ code=$create.code; status=$create.data.status; frozen=$acc2.data.frozen; bal=$acc2.data.balance; id=$bountyId }

$detail=Invoke-Api GET "/bounties/$bountyId" $null $tokenA
$wf=$detail.data.warrantFields
$emptyExtra=[string]::IsNullOrWhiteSpace([string]$wf.extra)
Pass 'AC-W2-data' ($wf.district -eq 'MedSchool' -and $emptyExtra) @{ district=$wf.district; extra=$wf.extra; acceptAgency=$wf.acceptAgency }

$adm=Invoke-Api POST '/admin/auth/login' '{"username":"admin","password":"admin123"}'
$at=$adm.data.token
$ap=Invoke-Api POST "/admin/bounty-reviews/$bountyId" '{"result":"APPROVE","reason":"ok"}' $at
if ($ap.code -ne 0) { $ap=Invoke-Api POST "/admin/bounty-reviews/$bountyId" '{"action":"APPROVE","comment":"ok"}' $at }
$d2=Invoke-Api GET "/bounties/$bountyId"
Pass 'AC-06' ($ap.code -eq 0 -and $d2.data.status -eq 'OPEN') @{ ap=$ap.code; apMsg=$ap.message; status=$d2.data.status }

$un=Invoke-Api GET '/wallet/account'
Pass 'AC-S1' ($un.code -eq 40100) @{ code=$un.code }
$h2a=Invoke-Api GET '/admin/dashboard/overview' $null $tokenA
$a2h=Invoke-Api GET '/auth/me' $null $at
Pass 'AC-S2' ($h2a.code -eq 40300 -and $a2h.code -eq 40300) @{ heroAdmin=$h2a.code; adminHero=$a2h.code }
$hall=Invoke-Api GET '/hall/bounty-reviews' $null $tokenA
Pass 'AC-S3' ($hall.code -eq 40310 -or $hall.code -eq 40300) @{ code=$hall.code; msg=$hall.message }

$self=Invoke-Api POST "/bounties/$bountyId/claims" '{}' $tokenA
$claim=Invoke-Api POST "/bounties/$bountyId/claims" '{}' $tokenB
$dup=Invoke-Api POST "/bounties/$bountyId/claims" '{}' $tokenB
Pass 'AC-07' ($self.code -ne 0 -and $claim.code -eq 0 -and $dup.code -ne 0) @{ self=$self.code; claim=$claim.code; dup=$dup.code; selfMsg=$self.message; claimMsg=$claim.message }

$msg=Invoke-Api POST "/bounties/$bountyId/messages" '{"content":"hello"}' $tokenB
$msgs=Invoke-Api GET "/bounties/$bountyId/messages?page=1&pageSize=20" $null $tokenA
$d3=Invoke-Api GET "/bounties/$bountyId" $null $tokenB
$itemParts=@()
foreach($c in @($d3.data.checklist)) {
  $itemParts += "{`"itemCode`":`"$($c.itemCode)`",`"done`":true,`"text`":`"done`",`"mediaUrls`":[]}"
}
if ($itemParts.Count -eq 0) {
  $itemParts=@('{"itemCode":"VERIFY_AUTHENTIC","done":true,"text":"done","mediaUrls":[]}','{"itemCode":"SITE_VISIT_RECORD","done":true,"text":"done","mediaUrls":[]}')
}
$itemsJson='['+($itemParts -join ',')+']'
$subBody="{`"summary`":`"done`",`"items`":$itemsJson}"
$sub=Invoke-Api POST "/bounties/$bountyId/submissions" $subBody $tokenB
$subId=$sub.data.id
if (-not $subId) { $subId=$sub.data.submissionId }
Pass 'AC-08' ($msg.code -eq 0 -and $msgs.code -eq 0 -and $sub.code -eq 0) @{ msg=$msg.code; msgs=$msgs.code; sub=$sub.code; subMsg=$sub.message; subId=$subId }

$sr=Invoke-Api POST "/admin/submission-reviews/$subId" '{"result":"APPROVE","reason":"ok"}' $at
if ($sr.code -ne 0) { $sr=Invoke-Api POST "/admin/submission-reviews/$subId" '{"action":"APPROVE","comment":"ok"}' $at }
$prev=Invoke-Api GET "/bounties/$bountyId/settlement/preview" $null $tokenA
$dist=$prev.data.distributable
$fee=$prev.data.fee
$settleBody="{`"items`":[{`"userId`":$idB,`"amount`":$dist,`"chivalryBonus`":0}]}"
$settle=Invoke-Api POST "/bounties/$bountyId/settlement" $settleBody $tokenA
$d4=Invoke-Api GET "/bounties/$bountyId"
$accB=Invoke-Api GET '/wallet/account' $null $tokenB
$feeOk=($null -ne $fee -and [decimal]$fee -eq 20)
Pass 'AC-09' ($sr.code -eq 0 -and $prev.code -eq 0 -and $feeOk -and $settle.code -eq 0 -and $d4.data.status -eq 'COMPLETED') @{ sr=$sr.code; srMsg=$sr.message; fee=$fee; dist=$dist; settle=$settle.code; settleMsg=$settle.message; status=$d4.data.status; balB=$accB.data.balance }

$ev1=Invoke-Api POST "/bounties/$bountyId/evaluations" "{`"toUserId`":$idB,`"score`":5,`"content`":`"good`"}" $tokenA
$ev2=Invoke-Api POST "/bounties/$bountyId/evaluations" "{`"toUserId`":$idA,`"score`":5,`"content`":`"good`"}" $tokenB
$lv=Invoke-Api GET '/growth/level' $null $tokenB
Pass 'AC-10' ($ev1.code -eq 0 -and $ev2.code -eq 0 -and $lv.code -eq 0) @{ ev1=$ev1.code; ev2=$ev2.code; level=$lv.data.level }

$rk=Invoke-Api GET '/ranks/reputation?page=1&pageSize=5' $null $tokenA
$of=Invoke-Api GET '/offices/defs' $null $tokenA
$ms=Invoke-Api GET '/messages?page=1&pageSize=10' $null $tokenB
$dm=Invoke-Api GET '/disputes/mine?page=1&pageSize=10' $null $tokenA
$dsp=Invoke-Api POST "/bounties/$bountyId/disputes" '{"reason":"qa dispute","evidenceText":"qa"}' $tokenA
Pass 'AC-X1' ($rk.code -eq 0) @{ code=$rk.code; msg=$rk.message }
Pass 'AC-X2' ($of.code -eq 0) @{ code=$of.code; msg=$of.message }
Pass 'AC-X3' ($dsp.code -eq 0) @{ disputeCreate=$dsp.code; disputeMsg=$dsp.message; mine=$dm.code }
Pass 'AC-X4' ($ms.code -eq 0) @{ code=$ms.code }

$wc1=Invoke-Api GET '/admin/warrant-field-configs' $null $at
$wc2=Invoke-Api GET '/admin/checklist-templates' $null $at
Pass 'AC-X5' ($wc1.code -eq 0 -or $wc2.code -eq 0) @{ warrant=$wc1.code; checklist=$wc2.code; wMsg=$wc1.message; cMsg=$wc2.message }

$detailVue=[System.IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\BountyDetailView.vue')
$pubVue=[System.IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\PublishBountyView.vue')
$noRaw=-not ($detailVue -match ':label="String\(key\)"')
$usesRows=($detailVue -match 'warrantRows') -and ($detailVue -match 'def\.label')
$pubLabel=$pubVue -match ':label="field\.label"'
$hideEmpty=$detailVue -match 'isWarrantValueEmpty'
Pass 'AC-W3' ($noRaw -and $usesRows) @{ noRaw=$noRaw; usesRows=$usesRows }
Pass 'AC-W4' ($pubLabel -and $usesRows) @{ pubLabel=$pubLabel }
Pass 'AC-W2' ($usesRows -and $hideEmpty -and $R['AC-W1'].pass) @{ usesRows=$usesRows; hideEmpty=$hideEmpty }
Pass 'AC-S4' $false @{ note='needs office grant; deferred' }

$R['meta']=@{ bountyId=$bountyId; idA=$idA; idB=$idB; suffix=$suffix; ts=(Get-Date).ToString('s') }
($R | ConvertTo-Json -Depth 8) | Set-Content "$outDir\e2e_results.json" -Encoding UTF8
$lines=@()
foreach($k in $R.Keys){ if($k -eq 'meta'){continue}; $lines += ("{0}={1}" -f $k, $(if($R[$k].pass){'PASS'}else{'FAIL'})) }
$lines | Set-Content "$outDir\e2e_summary.txt"
Write-Host 'DONE'
$lines
