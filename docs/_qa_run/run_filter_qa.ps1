# QA filter regression - ASCII only for Windows PowerShell
$ErrorActionPreference = "Continue"
$base = "http://localhost:8080/api/v1"
$outDir = "f:\Jinanghu_Ling\docs\_qa_run"
$results = [ordered]@{}

function Invoke-Api {
  param([string]$Method, [string]$Path, [string]$Body = "", [hashtable]$Headers = @{})
  $uri = $base + $Path
  try {
    if ($Method -eq "GET") {
      $r = Invoke-WebRequest -Method GET -Uri $uri -Headers $Headers -UseBasicParsing -TimeoutSec 20
    } else {
      $bytes = [Text.Encoding]::UTF8.GetBytes($Body)
      $r = Invoke-WebRequest -Method $Method -Uri $uri -Headers $Headers -ContentType "application/json; charset=utf-8" -Body $bytes -UseBasicParsing -TimeoutSec 20
    }
    return ($r.Content | ConvertFrom-Json)
  } catch {
    $resp = $_.Exception.Response
    if ($resp) {
      $sr = New-Object IO.StreamReader($resp.GetResponseStream(), [Text.Encoding]::UTF8)
      $txt = $sr.ReadToEnd()
      try { return ($txt | ConvertFrom-Json) } catch { return [pscustomobject]@{ code = -1; message = $txt } }
    }
    return [pscustomobject]@{ code = -1; message = $_.Exception.Message }
  }
}

function Set-Result([string]$Id, [bool]$Ok, $Detail) {
  $results[$Id] = @{ pass = $Ok; detail = $Detail }
  Write-Host ($Id + "=" + $(if ($Ok) { "PASS" } else { "FAIL" }))
}

$all = Invoke-Api -Method GET -Path "/bounties?page=1&pageSize=50&status=OPEN,IN_COLLAB"
Set-Result "PLAZA-BASE" ($all.code -eq 0) @{ code = $all.code; total = $all.data.total }
$sample = @($all.data.list)[0]
$typeSample = [string]$sample.type
if (-not $typeSample) { $typeSample = "RENT_SEEK" }
$titleHit = "xx"
if ($sample.title -and $sample.title.Length -ge 2) { $titleHit = $sample.title.Substring(0, 2) }
$districtSample = [string]$sample.district

$byType = Invoke-Api -Method GET -Path ("/bounties?page=1&pageSize=50&status=OPEN,IN_COLLAB&type=" + $typeSample)
$badType = @($byType.data.list | Where-Object { $_.type -ne $typeSample }).Count
Set-Result "PLAZA-TYPE" (($byType.code -eq 0) -and ($badType -eq 0)) @{ type = $typeSample; total = $byType.data.total; bad = $badType }

$seek = Invoke-Api -Method GET -Path "/bounties?page=1&pageSize=1&status=OPEN,IN_COLLAB&type=RENT_SEEK"
$out = Invoke-Api -Method GET -Path "/bounties?page=1&pageSize=1&status=OPEN,IN_COLLAB&type=RENT_OUT"
Set-Result "PLAZA-TYPE-BOTH" (($seek.code -eq 0) -and ($out.code -eq 0)) @{ seek = $seek.data.total; out = $out.data.total }

$kwEnc = [uri]::EscapeDataString($titleHit)
$byKw = Invoke-Api -Method GET -Path ("/bounties?page=1&pageSize=50&status=OPEN,IN_COLLAB&keyword=" + $kwEnc)
$badKw = @($byKw.data.list | Where-Object { $_.title -notlike ("*" + $titleHit + "*") }).Count
Set-Result "PLAZA-KEYWORD" (($byKw.code -eq 0) -and ($badKw -eq 0)) @{ keyword = $titleHit; total = $byKw.data.total; bad = $badKw }

$byEmpty = Invoke-Api -Method GET -Path "/bounties?page=1&pageSize=20&status=OPEN,IN_COLLAB&keyword=ZZZNOMATCH999"
Set-Result "PLAZA-KEYWORD-EMPTY" (($byEmpty.code -eq 0) -and ([int]$byEmpty.data.total -eq 0)) @{ total = $byEmpty.data.total }

if ($districtSample) {
  $dEnc = [uri]::EscapeDataString($districtSample)
  $byDist = Invoke-Api -Method GET -Path ("/bounties?page=1&pageSize=50&status=OPEN,IN_COLLAB&district=" + $dEnc)
  $badDist = @($byDist.data.list | Where-Object { $_.district -ne $districtSample }).Count
  Set-Result "PLAZA-DISTRICT" (($byDist.code -eq 0) -and ($badDist -eq 0)) @{ district = $districtSample; total = $byDist.data.total; bad = $badDist }
} else {
  Set-Result "PLAZA-DISTRICT" $true @{ note = "no district on sample" }
}

$combo = Invoke-Api -Method GET -Path ("/bounties?page=1&pageSize=50&status=OPEN,IN_COLLAB&type=" + $typeSample + "&keyword=" + $kwEnc)
$badCombo = @($combo.data.list | Where-Object { $_.type -ne $typeSample }).Count
Set-Result "PLAZA-COMBO" (($combo.code -eq 0) -and ($badCombo -eq 0)) @{ total = $combo.data.total; bad = $badCombo }

$nAll = Invoke-Api -Method GET -Path "/notices?page=1&pageSize=50"
Set-Result "NOTICE-ALL" ($nAll.code -eq 0) @{ total = $nAll.data.total }
foreach ($cat in @("ANTI_FRAUD", "RULE", "RENT_GUIDE", "ANNOUNCEMENT")) {
  $nc = Invoke-Api -Method GET -Path ("/notices?page=1&pageSize=50&category=" + $cat)
  $bad = @($nc.data.list | Where-Object { $_.category -ne $cat }).Count
  Set-Result ("NOTICE-" + $cat) (($nc.code -eq 0) -and ($bad -eq 0)) @{ total = $nc.data.total; bad = $bad }
}

foreach ($rt in @("REPUTATION", "CHIVALRY", "COMPLETED")) {
  $rk = Invoke-Api -Method GET -Path ("/ranks/" + $rt + "?page=1&pageSize=20")
  Set-Result ("RANK-" + $rt) ($rk.code -eq 0) @{ code = $rk.code; total = $rk.data.total }
}

$adminLogin = Invoke-Api -Method POST -Path "/admin/auth/login" -Body '{"username":"admin","password":"admin123"}'
$adminTok = [string]$adminLogin.data.token
$ah = @{ Authorization = ("Bearer " + $adminTok); "X-Admin" = "1" }
Set-Result "ADM-LOGIN" (($adminLogin.code -eq 0) -and ($adminTok.Length -gt 10)) @{ code = $adminLogin.code }

$inv = Invoke-Api -Method POST -Path "/admin/invites" -Body '{"count":1,"remark":"filter-qa"}' -Headers $ah
$inviteCode = $null
if ($inv.data.codes) { $inviteCode = [string](@($inv.data.codes)[0]) }
elseif ($inv.data.code) { $inviteCode = [string]$inv.data.code }
Set-Result "ADM-INVITE" (($inv.code -eq 0) -and $inviteCode) @{ code = $inv.code; invite = $inviteCode }

$suffix = Get-Random -Maximum 99999
$phone = ("139{0:D8}" -f (Get-Random -Maximum 99999999))
$username = "flt" + $suffix
$nickname = "NickFlt" + $suffix
$sms = Invoke-Api -Method POST -Path "/auth/sms/send" -Body ("{`"phone`":`"$phone`",`"scene`":`"REGISTER`"}")
$regObj = @{ inviteCode = $inviteCode; nickname = $nickname; phone = $phone; smsCode = "123456"; username = $username; password = "Test1234" }
$reg = Invoke-Api -Method POST -Path "/auth/register" -Body ($regObj | ConvertTo-Json -Compress)
if ($reg.code -ne 0) {
  # fallback open invites used by e2e seed
  foreach ($oc in @("JHOPEN1","JHOPEN2","JHOPEN3")) {
    $sms = Invoke-Api -Method POST -Path "/auth/sms/send" -Body ("{`"phone`":`"$phone`",`"scene`":`"REGISTER`"}")
    $regObj.inviteCode = $oc
    $reg = Invoke-Api -Method POST -Path "/auth/register" -Body ($regObj | ConvertTo-Json -Compress)
    if ($reg.code -eq 0) { break }
  }
}
$tokenU = [string]$reg.data.token
$uh = @{ Authorization = ("Bearer " + $tokenU) }
Set-Result "HERO-REG" (($reg.code -eq 0) -and ($tokenU.Length -gt 10)) @{ code = $reg.code }

$msgAll = Invoke-Api -Method GET -Path "/messages?page=1&pageSize=50" -Headers $uh
$msgUnread = Invoke-Api -Method GET -Path "/messages?page=1&pageSize=50&unreadOnly=true" -Headers $uh
$unreadBad = @($msgUnread.data.list | Where-Object {
  if ($null -ne $_.read) { $_.read -eq $true } elseif ($null -ne $_.readFlag) { $_.readFlag -eq $true } else { $false }
}).Count
Set-Result "MSG-UNREAD" (($msgAll.code -eq 0) -and ($msgUnread.code -eq 0) -and ($unreadBad -eq 0)) @{ all = $msgAll.data.total; unread = $msgUnread.data.total; bad = $unreadBad }

$pub = Invoke-Api -Method GET -Path "/bounties/mine/published?page=1&pageSize=20" -Headers $uh
$clm = Invoke-Api -Method GET -Path "/bounties/mine/claimed?page=1&pageSize=20" -Headers $uh
Set-Result "MINE-TABS" (($pub.code -eq 0) -and ($clm.code -eq 0)) @{ pub = $pub.data.total; claimed = $clm.data.total }

$usersAll = Invoke-Api -Method GET -Path "/admin/users?page=1&pageSize=20" -Headers $ah
Set-Result "ADM-USERS-BASE" ($usersAll.code -eq 0) @{ total = $usersAll.data.total }

$uByUser = Invoke-Api -Method GET -Path ("/admin/users?page=1&pageSize=20&keyword=" + [uri]::EscapeDataString($username)) -Headers $ah
Set-Result "ADM-USERS-KW-USERNAME" (($uByUser.code -eq 0) -and ([int]$uByUser.data.total -ge 1)) @{ total = $uByUser.data.total }

$uByPhone = Invoke-Api -Method GET -Path ("/admin/users?page=1&pageSize=20&keyword=" + [uri]::EscapeDataString($phone)) -Headers $ah
Set-Result "ADM-USERS-KW-PHONE" (($uByPhone.code -eq 0) -and ([int]$uByPhone.data.total -ge 1)) @{ total = $uByPhone.data.total }

$uByNick = Invoke-Api -Method GET -Path ("/admin/users?page=1&pageSize=20&keyword=" + [uri]::EscapeDataString($nickname)) -Headers $ah
Set-Result "ADM-USERS-KW-NICKNAME" (($uByNick.code -eq 0) -and ([int]$uByNick.data.total -ge 1)) @{ total = $uByNick.data.total; note = "UI claims nickname; backend username+phone" }

$abAll = Invoke-Api -Method GET -Path "/admin/bounties?page=1&pageSize=50" -Headers $ah
$abPend = Invoke-Api -Method GET -Path "/admin/bounties?page=1&pageSize=50&status=PENDING_REVIEW" -Headers $ah
$abBad = @($abPend.data.list | Where-Object { $_.status -ne "PENDING_REVIEW" }).Count
Set-Result "ADM-BOUNTY-STATUS" (($abAll.code -eq 0) -and ($abPend.code -eq 0) -and ($abBad -eq 0)) @{ all = $abAll.data.total; pending = $abPend.data.total; bad = $abBad }

$wAll = Invoke-Api -Method GET -Path "/admin/warrant-field-configs?page=1&pageSize=100" -Headers $ah
$wSeek = Invoke-Api -Method GET -Path "/admin/warrant-field-configs?page=1&pageSize=100&templateCode=RENT_SEEK" -Headers $ah
$wBad = @($wSeek.data.list | Where-Object { $_.templateCode -and $_.templateCode -ne "RENT_SEEK" }).Count
Set-Result "ADM-WARRANT-TPL" (($wAll.code -eq 0) -and ($wSeek.code -eq 0) -and ($wBad -eq 0)) @{ all = $wAll.data.total; seek = $wSeek.data.total; bad = $wBad }

$feMap = [ordered]@{
  "FE-PLAZA" = "f:\Jinanghu_Ling\frontend\src\views\hero\BountyPlazaView.vue"
  "FE-NOTICE" = "f:\Jinanghu_Ling\frontend\src\views\hero\NoticesView.vue"
  "FE-RANK" = "f:\Jinanghu_Ling\frontend\src\views\hero\RankView.vue"
  "FE-MSG" = "f:\Jinanghu_Ling\frontend\src\views\hero\MessagesView.vue"
  "FE-MINE" = "f:\Jinanghu_Ling\frontend\src\views\hero\MyBountiesView.vue"
  "FE-ADM-USERS" = "f:\Jinanghu_Ling\frontend\src\views\admin\UsersView.vue"
  "FE-ADM-BOUNTY" = "f:\Jinanghu_Ling\frontend\src\views\admin\BountiesView.vue"
  "FE-ADM-WARRANT" = "f:\Jinanghu_Ling\frontend\src\views\admin\WarrantConfigAdminView.vue"
}
$fePat = [ordered]@{
  "FE-PLAZA" = "keyword"
  "FE-NOTICE" = "category"
  "FE-RANK" = "getRanks"
  "FE-MSG" = "unreadOnly"
  "FE-MINE" = "listMyPublished"
  "FE-ADM-USERS" = "keyword"
  "FE-ADM-BOUNTY" = "query.status"
  "FE-ADM-WARRANT" = "templateCode"
}
foreach ($k in $feMap.Keys) {
  $ok = Select-String -Path $feMap[$k] -Pattern $fePat[$k] -Quiet
  Set-Result $k ([bool]$ok) @{ file = $feMap[$k] }
}

$results["meta"] = @{ ts = (Get-Date).ToString("s"); sampleId = $sample.id; username = $username; nickname = $nickname; phone = $phone }
$json = $results | ConvertTo-Json -Depth 8
[IO.File]::WriteAllText((Join-Path $outDir "filter_results.json"), $json, [Text.Encoding]::UTF8)
$lines = New-Object System.Collections.Generic.List[string]
foreach ($k in $results.Keys) {
  if ($k -eq "meta") { continue }
  [void]$lines.Add(($k + "=" + $(if ($results[$k].pass) { "PASS" } else { "FAIL" })))
}
[IO.File]::WriteAllText((Join-Path $outDir "filter_summary.txt"), ($lines -join "`r`n"), [Text.Encoding]::UTF8)
Write-Host "FILTER QA DONE"