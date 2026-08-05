# QA follow-up v2b - ASCII only
$ErrorActionPreference='Continue'
$base='http://localhost:8080/api/v1'
$outDir='f:\Jinanghu_Ling\docs\_qa_run'
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
  $inv=Invoke-Api POST '/admin/invites' '{"quota":5,"remark":"qa"}' $token
  $c=$null
  if ($inv.data.codes) { $c=@($inv.data.codes)[0] }
  elseif ($inv.data.code) { $c=$inv.data.code }
  return @{ resp=$inv; code=$c }
}

$adm=Invoke-Api POST '/admin/auth/login' '{"username":"admin","password":"admin123"}'
$at=$adm.data.token
$me=Invoke-Api GET '/admin/auth/me' $null $at
$roles=Invoke-Api GET '/admin/roles' $null $at
$roleCodes=@($roles.data | ForEach-Object { $_.code })
$ops=@($roles.data | Where-Object { $_.code -eq 'OPS_ADMIN' })[0]
$obs=@($roles.data | Where-Object { $_.code -eq 'OBSERVER' })[0]
$hasStarInOps=($ops.permissions -contains '*')
$hasStarInObs=($obs.permissions -contains '*')
$meRoleCodes=@($me.data.roles | ForEach-Object { $_.code })
$superStar=((@($me.data.permissions) -contains '*') -and ($meRoleCodes -contains 'SUPER_ADMIN'))
Pass 'D-003' ($adm.code -eq 0 -and $roles.code -eq 0 -and $roleCodes.Count -ge 4 -and $superStar -and (-not $hasStarInOps) -and (-not $hasStarInObs) -and ($ops.permissions.Count -gt 5)) @{
  meRoles=$meRoleCodes; mePerms=@($me.data.permissions); roleCodes=$roleCodes
  opsPermCount=$ops.permissions.Count; obsPermCount=$obs.permissions.Count
}

$suffix=Get-Random -Minimum 10000 -Maximum 99999
$i1=Invite-Code $at
$i2=Invite-Code $at
$i3=Invite-Code $at
$phoneP=('137{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$phoneR=('136{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$phoneC=('135{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$userP="qsp$suffix"; $userR="qsr$suffix"; $userC="qsc$suffix"

Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneP`",`"scene`":`"REGISTER`"}" | Out-Null
$regP=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$($i1.code)`",`"phone`":`"$phoneP`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userP`",`"nickname`":`"Pub$suffix`"}"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneR`",`"scene`":`"REGISTER`"}" | Out-Null
$regR=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$($i2.code)`",`"phone`":`"$phoneR`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userR`",`"nickname`":`"Rev$suffix`"}"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneC`",`"scene`":`"REGISTER`"}" | Out-Null
$regC=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$($i3.code)`",`"phone`":`"$phoneC`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userC`",`"nickname`":`"Clm$suffix`"}"
$idP=$regP.data.user.id; $idR=$regR.data.user.id; $idC=$regC.data.user.id
Pass 'REG' ($regP.code -eq 0 -and $regR.code -eq 0 -and $regC.code -eq 0) @{ P=$regP.code; R=$regR.code; C=$regC.code; idP=$idP; idR=$idR; idC=$idC; i1=$i1.code; i2=$i2.code; i3=$i3.code }

$mysql='C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$mp='Admin@123'
$sql="INSERT INTO user_office (user_id, office_code, status, start_at, end_at) VALUES ($idP,'DECREE_REVIEWER','ACTIVE',NOW(),DATE_ADD(NOW(),INTERVAL 90 DAY)),($idR,'DECREE_REVIEWER','ACTIVE',NOW(),DATE_ADD(NOW(),INTERVAL 90 DAY)),($idR,'FEAT_REVIEWER','ACTIVE',NOW(),DATE_ADD(NOW(),INTERVAL 90 DAY)),($idC,'FEAT_REVIEWER','ACTIVE',NOW(),DATE_ADD(NOW(),INTERVAL 90 DAY));"
$grantOut=& $mysql -uroot "-p$mp" --default-character-set=utf8mb4 jianghu_ling -e $sql 2>&1 | Out-String
Pass 'SQL-GRANT' ($grantOut -notmatch 'ERROR 1') @{ out=($grantOut -replace '[\r\n]+',' ').Substring(0,[Math]::Min(200,$grantOut.Length)) }

$tokenP=(Invoke-Api POST '/auth/login' "{`"loginType`":`"PASSWORD`",`"username`":`"$userP`",`"password`":`"Test1234`"}").data.token
$tokenR=(Invoke-Api POST '/auth/login' "{`"loginType`":`"PASSWORD`",`"username`":`"$userR`",`"password`":`"Test1234`"}").data.token
$tokenC=(Invoke-Api POST '/auth/login' "{`"loginType`":`"PASSWORD`",`"username`":`"$userC`",`"password`":`"Test1234`"}").data.token
$meP=Invoke-Api GET '/auth/me' $null $tokenP
$meR=Invoke-Api GET '/auth/me' $null $tokenR
$pOff=@($meP.data.offices | ForEach-Object { $_.code })
$rOff=@($meR.data.offices | ForEach-Object { $_.code })
Pass 'OFFICE-LOADED' (($pOff.Count -ge 1) -and ($rOff.Count -ge 2)) @{ p=$pOff; r=$rOff }

Invoke-Api POST '/wallet/recharge' "{`"amount`":500,`"clientRequestId`":`"s4-$suffix`"}" $tokenP | Out-Null
$deadline=(Get-Date).ToUniversalTime().AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$bjson='{"type":"RENT_SEEK","title":"S4-'+$suffix+'","difficulty":"EASY","rewardAmount":200,"confirmLowReward":false,"deadlineAt":"'+$deadline+'","taskTags":["t"],"checklistItemCodes":["VERIFY_AUTHENTIC","SITE_VISIT_RECORD"],"warrantFields":{"district":"H","rentBudgetMin":800,"rentBudgetMax":1000,"layout":"2","expectMoveInDate":"2026-09-01","acceptAgency":false,"extra":""}}'
$create=Invoke-Api POST '/bounties' $bjson $tokenP
$bid=$create.data.id
$queue=Invoke-Api GET '/hall/bounty-reviews?page=1&pageSize=20' $null $tokenP
$avoid=Invoke-Api POST ("/hall/bounty-reviews/"+$bid) '{"result":"APPROVE","reason":"self"}' $tokenP
# True AC-S4: has office (queue readable) AND review own bounty forbidden
Pass 'AC-S4' (($pOff.Count -ge 1) -and ($queue.code -eq 0) -and ($avoid.code -eq 40310)) @{
  queue=$queue.code; avoid=$avoid.code; avoidMsg=$avoid.message; offices=$pOff; bountyId=$bid
}

$okRev=Invoke-Api POST ("/hall/bounty-reviews/"+$bid) '{"result":"APPROVE","reason":"ok"}' $tokenR
$st=(Invoke-Api GET ("/bounties/"+$bid)).data.status
Pass 'HALL-OK' (($okRev.code -eq 0) -and ($st -eq 'OPEN')) @{ code=$okRev.code; msg=$okRev.message; status=$st }

$claim=Invoke-Api POST ("/bounties/"+$bid+"/claims") '{}' $tokenC
$d=Invoke-Api GET ("/bounties/"+$bid) $null $tokenC
$clist=@($d.data.checklist)
$itemJson='['
for ($i=0; $i -lt $clist.Count; $i++) {
  if ($i -gt 0) { $itemJson += ',' }
  $itemJson += '{"itemCode":"'+$clist[$i].itemCode+'","done":true,"text":"d","mediaUrls":[]}'
}
if ($clist.Count -eq 0) {
  $itemJson='[{"itemCode":"VERIFY_AUTHENTIC","done":true,"text":"d","mediaUrls":[]},{"itemCode":"SITE_VISIT_RECORD","done":true,"text":"d","mediaUrls":[]}]'
} else { $itemJson += ']' }
$sub=Invoke-Api POST ("/bounties/"+$bid+"/submissions") ('{"summary":"d","items":'+$itemJson+'}') $tokenC
$sid=$sub.data.id
if (-not $sid) { $sid=$sub.data.submissionId }
$featAvoid=Invoke-Api POST ("/hall/submission-reviews/"+$sid) '{"result":"APPROVE","reason":"self"}' $tokenC
Pass 'AC-S4-FEAT' (($featAvoid.code -eq 40310) -and ($sub.code -eq 0)) @{ code=$featAvoid.code; msg=$featAvoid.message; sub=$sub.code; sid=$sid; claim=$claim.code }

$featOk=Invoke-Api POST ("/hall/submission-reviews/"+$sid) '{"result":"APPROVE","reason":"ok"}' $tokenR
Pass 'FEAT-OK' ($featOk.code -eq 0) @{ code=$featOk.code; msg=$featOk.message }

$prev=Invoke-Api GET ("/bounties/"+$bid+"/settlement/preview") $null $tokenP
$dist=$prev.data.distributable
$settle=Invoke-Api POST ("/bounties/"+$bid+"/settlement") ('{"items":[{"userId":'+$idC+',"amount":'+$dist+',"chivalryBonus":0}]}') $tokenP
$dsp=Invoke-Api POST ("/bounties/"+$bid+"/disputes") '{"reason":"qa","evidenceText":"x"}' $tokenP
$dspId=$dsp.data.id
$ver=Invoke-Api POST ("/admin/disputes/"+$dspId+"/verdict") '{"action":"KEEP","comment":"qa keep"}' $at
Pass 'DISPUTE-VERDICT' (($settle.code -eq 0) -and ($dsp.code -eq 0) -and ($ver.code -eq 0)) @{
  settle=$settle.code; dispute=$dsp.code; dspId=$dspId; verdict=$ver.code; verMsg=$ver.message
}

$R['meta']=@{bid=$bid;idP=$idP;idR=$idR;idC=$idC;suffix=$suffix;ts=(Get-Date).ToString('s')}
($R|ConvertTo-Json -Depth 8)|Set-Content "$outDir\followup_v2_results.json" -Encoding UTF8
$lines=@(); foreach($k in $R.Keys){ if($k -eq 'meta'){continue}; $lines += ("{0}={1}" -f $k,$(if($R[$k].pass){'PASS'}else{'FAIL'})) }
$lines|Set-Content "$outDir\followup_v2_summary.txt"
Write-Host 'V2B DONE'
$lines
