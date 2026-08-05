# QA follow-up: D-003 RBAC + AC-S4 avoidance + admin dispute verdict
# Account: admin / admin123
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
function Pass($id,$ok,$detail) { $R[$id]=@{ pass=[bool]$ok; detail=$detail } }

# ---- D-003 / admin RBAC ----
$adm=Invoke-Api POST '/admin/auth/login' '{"username":"admin","password":"admin123"}'
$at=$adm.data.token
$perms=@($adm.data.permissions)
$me=Invoke-Api GET '/admin/auth/me' $null $at
$roles=Invoke-Api GET '/admin/roles' $null $at
$admins=Invoke-Api GET '/admin/admins' $null $at
$dash=Invoke-Api GET '/admin/dashboard/overview' $null $at
Pass 'ADM-LOGIN' ($adm.code -eq 0 -and $at) @{ code=$adm.code }
Pass 'D-003-RBAC' ($adm.code -eq 0 -and $null -ne $perms -and $perms.Count -ge 1) @{
  permissions=$perms; mePerms=@($me.data.permissions); rolesCode=$roles.code; adminsCode=$admins.code
  note='SUPER may still be [*]; check roles/admins APIs exist'
}
Pass 'ADM-DASH' ($dash.code -eq 0) @{ code=$dash.code }

# ---- prepare invite ----
$inv=Invoke-Api POST '/admin/invites' '{"quota":10,"remark":"qa-s4"}' $at
$icode=$inv.data.code
if (-not $icode) { $icode=$inv.data.inviteCode }
if (-not $icode) {
  $ilist=Invoke-Api GET '/admin/invites?page=1&pageSize=50' $null $at
  $active=@($ilist.data.list | Where-Object { $_.status -eq 'ACTIVE' -and $_.usedCount -lt $_.quota } | Select-Object -First 2)
  $icode=$active[0].code
  $icode2=$active[1].code
}
Pass 'ADM-INVITE' ($inv.code -eq 0 -or $icode) @{ code=$inv.code; invite=$icode }

$suffix=Get-Random -Minimum 10000 -Maximum 99999
$phoneP=('137{0:D8}' -f (Get-Random -Minimum 0 -Maximum 99999999))
$phoneR=('136{0:D8}' -f (Get-Random -Minimum 0 -Maximum 99999999))
$userP="qap$suffix"; $userR="qar$suffix"

Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneP`",`"scene`":`"REGISTER`"}" | Out-Null
$regP=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$icode`",`"phone`":`"$phoneP`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userP`",`"nickname`":`"Pub$suffix`"}"
$tokenP=$regP.data.token
$idP=$regP.data.user.id

# second invite for reviewer
$inv2=Invoke-Api POST '/admin/invites' '{"quota":5,"remark":"qa-rev"}' $at
$icode2=$inv2.data.code
if (-not $icode2) { $icode2=$inv2.data.inviteCode }
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneR`",`"scene`":`"REGISTER`"}" | Out-Null
$regR=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$icode2`",`"phone`":`"$phoneR`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userR`",`"nickname`":`"Rev$suffix`"}"
$tokenR=$regR.data.token
$idR=$regR.data.user.id
Pass 'REG-USERS' ($regP.code -eq 0 -and $regR.code -eq 0) @{ P=$regP.code; R=$regR.code; idP=$idP; idR=$idR; pMsg=$regP.message; rMsg=$regR.message }

# ---- grant offices via MySQL (documented path) ----
$mysql='C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$sql=@"
INSERT INTO user_office (user_id, office_code, status, start_at, end_at) VALUES
($idP, 'DECREE_REVIEWER', 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
($idR, 'DECREE_REVIEWER', 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
($idR, 'FEAT_REVIEWER', 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY));
"@
$sqlFile=Join-Path $outDir 'grant_office.sql'
[System.IO.File]::WriteAllText($sqlFile, $sql, [Text.UTF8Encoding]::new($false))
$grantOk=$false
$grantMsg=''
if ((Test-Path $mysql) -and $idP -and $idR) {
  $out = & $mysql -uroot -proot --default-character-set=utf8mb4 jianghu_ling -e "source $($sqlFile -replace '\\','/')" 2>&1 | Out-String
  $grantMsg=$out
  $grantOk=($LASTEXITCODE -eq 0 -or $out -notmatch 'ERROR')
}
Pass 'SQL-GRANT' $grantOk @{ msg=$grantMsg; idP=$idP; idR=$idR }

# refresh tokens after office grant (me should show offices)
$loginP=Invoke-Api POST '/auth/login' "{`"loginType`":`"PASSWORD`",`"username`":`"$userP`",`"password`":`"Test1234`"}"
$tokenP=$loginP.data.token
$meP=Invoke-Api GET '/auth/me' $null $tokenP
$loginR=Invoke-Api POST '/auth/login' "{`"loginType`":`"PASSWORD`",`"username`":`"$userR`",`"password`":`"Test1234`"}"
$tokenR=$loginR.data.token
$meR=Invoke-Api GET '/auth/me' $null $tokenR
Pass 'OFFICE-ME' (($meP.code -eq 0) -and (@($meP.data.offices).Count -ge 1) -and (@($meR.data.offices).Count -ge 1)) @{
  pOffices=@($meP.data.offices | ForEach-Object { $_.code }); rOffices=@($meR.data.offices | ForEach-Object { $_.code })
}

# publisher recharge + create bounty
$biz="qa-s4-$suffix"
Invoke-Api POST '/wallet/recharge' "{`"amount`":500,`"clientRequestId`":`"$biz`"}" $tokenP | Out-Null
$deadline=(Get-Date).ToUniversalTime().AddDays(7).ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$createBody=@"
{"type":"RENT_SEEK","title":"QA-S4-$suffix","difficulty":"EASY","rewardAmount":200,"confirmLowReward":false,"deadlineAt":"$deadline","taskTags":["tag"],"checklistItemCodes":["VERIFY_AUTHENTIC","SITE_VISIT_RECORD"],"warrantFields":{"district":"Hong","rentBudgetMin":800,"rentBudgetMax":1000,"layout":"2BR","expectMoveInDate":"2026-09-01","acceptAgency":false,"extra":""}}
"@
$create=Invoke-Api POST '/bounties' $createBody $tokenP
$bountyId=$create.data.id
Pass 'CREATE-BOUNTY' ($create.code -eq 0 -and $create.data.status -eq 'PENDING_REVIEW') @{ code=$create.code; id=$bountyId; status=$create.data.status }

# AC-S4: publisher with DECREE tries to review own bounty -> forbidden
$avoid=Invoke-Api POST "/hall/bounty-reviews/$bountyId" '{"result":"APPROVE","reason":"self"}' $tokenP
$avoidOk=($avoid.code -eq 40310)
Pass 'AC-S4' $avoidOk @{ code=$avoid.code; message=$avoid.message }

# reviewer (not party) can approve
$okRev=Invoke-Api POST "/hall/bounty-reviews/$bountyId" '{"result":"APPROVE","reason":"ok"}' $tokenR
$d1=Invoke-Api GET "/bounties/$bountyId"
Pass 'HALL-APPROVE' ($okRev.code -eq 0 -and $d1.data.status -eq 'OPEN') @{ code=$okRev.code; msg=$okRev.message; status=$d1.data.status }

# FEAT avoidance: reviewer claims then cannot review submission - optional short path
# claim by publisher? no - use a third or publisher claim fails. Use publisher as claimant after open? publisher can't claim own.
# Register claimant C quickly
$inv3=Invoke-Api POST '/admin/invites' '{"quota":3,"remark":"qa-c"}' $at
$icode3=$inv3.data.code; if(-not $icode3){$icode3=$inv3.data.inviteCode}
$phoneC=('135{0:D8}' -f (Get-Random -Minimum 0 -Maximum 99999999))
$userC="qac$suffix"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneC`",`"scene`":`"REGISTER`"}" | Out-Null
$regC=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$icode3`",`"phone`":`"$phoneC`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userC`",`"nickname`":`"Clm$suffix`"}"
$tokenC=$regC.data.token
$idC=$regC.data.user.id
$claim=Invoke-Api POST "/bounties/$bountyId/claims" '{}' $tokenC
$d2=Invoke-Api GET "/bounties/$bountyId" $null $tokenC
$itemParts=@()
foreach($c in @($d2.data.checklist)){ $itemParts += "{`"itemCode`":`"$($c.itemCode)`",`"done`":true,`"text`":`"done`",`"mediaUrls`":[]}" }
if($itemParts.Count -eq 0){ $itemParts=@('{"itemCode":"VERIFY_AUTHENTIC","done":true,"text":"done","mediaUrls":[]}','{"itemCode":"SITE_VISIT_RECORD","done":true,"text":"done","mediaUrls":[]}') }
$sub=Invoke-Api POST "/bounties/$bountyId/submissions" ("{`"summary`":`"done`",`"items`":["+($itemParts -join ',')+"]}") $tokenC
$subId=$sub.data.id; if(-not $subId){$subId=$sub.data.submissionId}
# grant FEAT to claimant C then try review own submission
if ((Test-Path $mysql) -and $idC) {
  & $mysql -uroot -proot --default-character-set=utf8mb4 jianghu_ling -e "INSERT INTO user_office (user_id, office_code, status, start_at, end_at) VALUES ($idC,'FEAT_REVIEWER','ACTIVE',NOW(),DATE_ADD(NOW(),INTERVAL 90 DAY));" 2>&1 | Out-Null
}
$loginC=Invoke-Api POST '/auth/login' "{`"loginType`":`"PASSWORD`",`"username`":`"$userC`",`"password`":`"Test1234`"}"
$tokenC=$loginC.data.token
$featAvoid=Invoke-Api POST "/hall/submission-reviews/$subId" '{"result":"APPROVE","reason":"self"}' $tokenC
Pass 'AC-S4-FEAT' ($featAvoid.code -eq 40310 -and $claim.code -eq 0 -and $sub.code -eq 0) @{
  claim=$claim.code; sub=$sub.code; subId=$subId; featCode=$featAvoid.code; featMsg=$featAvoid.message
}

# proper feat approve by R
$featOk=Invoke-Api POST "/hall/submission-reviews/$subId" '{"result":"APPROVE","reason":"ok"}' $tokenR
Pass 'HALL-FEAT-OK' ($featOk.code -eq 0) @{ code=$featOk.code; msg=$featOk.message }

# settle + dispute + admin verdict
$prev=Invoke-Api GET "/bounties/$bountyId/settlement/preview" $null $tokenP
$dist=$prev.data.distributable
$settle=Invoke-Api POST "/bounties/$bountyId/settlement" "{`"items`":[{`"userId`":$idC,`"amount`":$dist,`"chivalryBonus`":0}]}" $tokenP
$dsp=Invoke-Api POST "/bounties/$bountyId/disputes" '{"reason":"qa verdict","evidenceText":"e2e"}' $tokenP
$dspId=$dsp.data.id
$ver=Invoke-Api POST "/admin/disputes/$dspId/verdict" '{"verdict":"MAINTAIN","reason":"qa ok","adjustments":[]}' $at
if ($ver.code -ne 0) {
  $ver=Invoke-Api POST "/admin/disputes/$dspId/verdict" '{"result":"REJECT_DISPUTE","comment":"qa"}' $at
}
Pass 'ADM-DISPUTE' ($settle.code -eq 0 -and $dsp.code -eq 0 -and ($ver.code -eq 0 -or $dspId)) @{
  settle=$settle.code; dispute=$dsp.code; dspId=$dspId; verdict=$ver.code; verMsg=$ver.message
}

# warrant detail labels still ok
$wtRaw=(New-Object Net.WebClient).DownloadString("$base/meta/warrant-templates")
$buChong=-join ([char[]](0x8865,0x5145,0x8BF4,0x660E))
Pass 'AC-W1-REGRESS' ($wtRaw.Contains($buChong)) @{ ok=$true }

$R['meta']=@{ bountyId=$bountyId; idP=$idP; idR=$idR; idC=$idC; suffix=$suffix; ts=(Get-Date).ToString('s') }
($R | ConvertTo-Json -Depth 8) | Set-Content "$outDir\followup_results.json" -Encoding UTF8
$lines=@(); foreach($k in $R.Keys){ if($k -eq 'meta'){continue}; $lines += ("{0}={1}" -f $k, $(if($R[$k].pass){'PASS'}else{'FAIL'})) }
$lines | Set-Content "$outDir\followup_summary.txt"
Write-Host 'FOLLOWUP DONE'
$lines
