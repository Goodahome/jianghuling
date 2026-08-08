# QA §9.14 / api §7.9 lifecycle - ASCII BOM for Windows PS
$ErrorActionPreference = "Continue"
$base = "http://localhost:8080/api/v1"
$outDir = "f:\Jinanghu_Ling\docs\_qa_run"
$results = [ordered]@{}

function Invoke-Api {
  param([string]$Method, [string]$Path, [string]$Body = "", [hashtable]$Headers = @{})
  $uri = $base + $Path
  try {
    if ($Method -eq "GET") {
      $r = Invoke-WebRequest -Method GET -Uri $uri -Headers $Headers -UseBasicParsing -TimeoutSec 25
    } else {
      $bytes = [Text.Encoding]::UTF8.GetBytes($Body)
      $r = Invoke-WebRequest -Method $Method -Uri $uri -Headers $Headers -ContentType "application/json; charset=utf-8" -Body $bytes -UseBasicParsing -TimeoutSec 25
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

function Caps-Eq($caps, $key, $expect) {
  return ([bool]$caps.$key) -eq $expect
}

# --- bootstrap ---
$adm = Invoke-Api POST "/admin/auth/login" '{"username":"admin","password":"admin123"}'
$ah = @{ Authorization = ("Bearer " + $adm.data.token); "X-Admin" = "1" }
Set-Result "ADM-LOGIN" (($adm.code -eq 0) -and $adm.data.token) @{ code = $adm.code }

$inv = Invoke-Api POST "/admin/invites" '{"count":2,"remark":"v1810-lc"}' $ah
$c1 = [string](@($inv.data.codes)[0]); $c2 = [string](@($inv.data.codes)[1])
$s = Get-Random -Maximum 99999
$p1 = ("136{0:D8}" -f (Get-Random -Maximum 99999999))
$p2 = ("135{0:D8}" -f (Get-Random -Maximum 99999999))
Invoke-Api POST "/auth/sms/send" ("{`"phone`":`"$p1`",`"scene`":`"REGISTER`"}") | Out-Null
$regP = Invoke-Api POST "/auth/register" ((@{inviteCode=$c1;nickname=("PubLC"+$s);phone=$p1;smsCode="123456";username=("publc"+$s);password="Test1234"} | ConvertTo-Json -Compress))
Invoke-Api POST "/auth/sms/send" ("{`"phone`":`"$p2`",`"scene`":`"REGISTER`"}") | Out-Null
$regC = Invoke-Api POST "/auth/register" ((@{inviteCode=$c2;nickname=("ClmLC"+$s);phone=$p2;smsCode="123456";username=("clmlc"+$s);password="Test1234"} | ConvertTo-Json -Compress))
$hP = @{ Authorization = ("Bearer " + $regP.data.token) }
$hC = @{ Authorization = ("Bearer " + $regC.data.token) }
Set-Result "REG" (($regP.code -eq 0) -and ($regC.code -eq 0)) @{ P = $regP.code; C = $regC.code }

$dl = (Get-Date).ToUniversalTime().AddDays(10).ToString("yyyy-MM-ddTHH:mm:ss'Z'")
$createBody = @{
  type = "RENT_SEEK"; title = ("LC-" + $s); difficulty = "NORMAL"; rewardAmount = 200; confirmLowReward = $true
  deadlineAt = $dl; taskTags = @("帮寻房")
  warrantFields = @{ district = "汇川"; rentBudgetMin = 1000; rentBudgetMax = 2000; layout = "两室"; expectMoveInDate = "2026-09-01"; acceptAgency = $false; extra = "qa" }
  checklistItemCodes = @("VERIFY_AUTHENTIC","SITE_VISIT_RECORD","PHOTO_EVIDENCE")
} | ConvertTo-Json -Compress -Depth 6
$cr = Invoke-Api POST "/bounties" $createBody $hP
$bid = $cr.data.id
Set-Result "CREATE" (($cr.code -eq 0) -and $bid -and (@($cr.data.checklist).Count -ge 1)) @{ id = $bid; status = $cr.data.status; checklist = @($cr.data.checklist).Count }

$rev = Invoke-Api POST ("/admin/bounty-reviews/" + $bid) '{"result":"APPROVE","reason":"ok"}' $ah
Set-Result "APPROVE" ($rev.code -eq 0) @{ code = $rev.code; msg = $rev.message }

$cl = Invoke-Api POST ("/bounties/" + $bid + "/claims") "{}" $hC
Set-Result "CLAIM" ($cl.code -eq 0) @{ code = $cl.code; status = $cl.data.status }

# --- 1) publisher IN_COLLAB capabilities + FE field presence ---
$detP = Invoke-Api GET ("/bounties/" + $bid) "" $hP
$capP = $detP.data.capabilities
$apiKeys = @("canCancel","canSendMessage","canReadMessages","canViewSubmissions","canSubmit","canSettle","canQuitClaim","canRepublish","canDispute")
$missing = @($apiKeys | Where-Object { $null -eq $capP.$_ })
$pubOk = ($detP.data.status -eq "IN_COLLAB") -and ($missing.Count -eq 0) `
  -and (Caps-Eq $capP "canCancel" $true) -and (Caps-Eq $capP "canSendMessage" $true) `
  -and (Caps-Eq $capP "canReadMessages" $true) -and (Caps-Eq $capP "canViewSubmissions" $true) `
  -and (Caps-Eq $capP "canSubmit" $false) -and (Caps-Eq $capP "canQuitClaim" $false)
Set-Result "PUB-CAPS-IN_COLLAB" $pubOk @{ status = $detP.data.status; caps = $capP; missing = $missing }

# FE wiring static
$feDetail = Select-String -Path "f:\Jinanghu_Ling\frontend\src\views\hero\BountyDetailView.vue" -Pattern "caps.canCancel|caps.canSettle|caps.canViewSubmissions|caps.canQuitClaim|协作会话|成果查看|完结分配|取消悬赏" -Quiet
$feTypes = Select-String -Path "f:\Jinanghu_Ling\frontend\src\types\models.ts" -Pattern "canSendMessage|canViewSubmissions|canQuitClaim" -Quiet
Set-Result "FE-DETAIL-CAPS" (($feDetail) -and ($feTypes)) @{ detail = [bool]$feDetail; types = [bool]$feTypes }

# --- 2) claimer IN_COLLAB: chat mutual, submit, quit ---
$detC = Invoke-Api GET ("/bounties/" + $bid) "" $hC
$capC = $detC.data.capabilities
$clmOk = (Caps-Eq $capC "canSendMessage" $true) -and (Caps-Eq $capC "canSubmit" $true) -and (Caps-Eq $capC "canQuitClaim" $true) -and (Caps-Eq $capC "canCancel" $false)
Set-Result "CLM-CAPS-IN_COLLAB" $clmOk @{ caps = $capC }

$m1 = Invoke-Api POST ("/bounties/" + $bid + "/messages") '{"content":"msg-from-publisher"}' $hP
$m2 = Invoke-Api POST ("/bounties/" + $bid + "/messages") '{"content":"msg-from-claimer"}' $hC
$listP = Invoke-Api GET ("/bounties/" + $bid + "/messages?page=1&pageSize=50") "" $hP
$listC = Invoke-Api GET ("/bounties/" + $bid + "/messages?page=1&pageSize=50") "" $hC
$tp = (@($listP.data.list | ForEach-Object { $_.content }) -join "|")
$tc = (@($listC.data.list | ForEach-Object { $_.content }) -join "|")
$mutual = ($m1.code -eq 0) -and ($m2.code -eq 0) -and ($tp -like "*msg-from-publisher*") -and ($tp -like "*msg-from-claimer*") -and ($tc -like "*msg-from-publisher*") -and ($tc -like "*msg-from-claimer*")
Set-Result "CHAT-MUTUAL" $mutual @{ m1 = $m1.code; m2 = $m2.code; totalP = $listP.data.total; totalC = $listC.data.total }

# submit as claimer
$subBody = '{"summary":"qa-sub","items":[{"itemCode":"VERIFY_AUTHENTIC","done":true,"text":"ok","mediaUrls":[]}]}'
$sub = Invoke-Api POST ("/bounties/" + $bid + "/submissions") $subBody $hC
Set-Result "SUBMIT-OK" ($sub.code -eq 0) @{ code = $sub.code; msg = $sub.message; id = $sub.data.id }

# list submissions for publisher
$subs = Invoke-Api GET ("/bounties/" + $bid + "/submissions?page=1&pageSize=20") "" $hP
Set-Result "PUB-VIEW-SUBS" (($subs.code -eq 0) -and ([int]$subs.data.total -ge 1)) @{ code = $subs.code; total = $subs.data.total }

# quit claim
$quit = Invoke-Api POST ("/bounties/" + $bid + "/claims/quit") '{"reason":"qa-quit"}' $hC
$detC2 = Invoke-Api GET ("/bounties/" + $bid) "" $hC
$capQ = $detC2.data.capabilities
$msgAfterQuit = Invoke-Api POST ("/bounties/" + $bid + "/messages") '{"content":"should-fail-after-quit"}' $hC
$subAfterQuit = Invoke-Api POST ("/bounties/" + $bid + "/submissions") $subBody $hC
$hist = Invoke-Api GET ("/bounties/" + $bid + "/messages?page=1&pageSize=50") "" $hC
$quitOk = ($quit.code -eq 0) -and ($quit.data.status -eq "QUIT") `
  -and (-not [bool]$capQ.canSendMessage) -and (-not [bool]$capQ.canSubmit) -and (-not [bool]$capQ.canQuitClaim) `
  -and ([bool]$capQ.canReadMessages) `
  -and (($msgAfterQuit.code -eq 40300) -or ($msgAfterQuit.code -eq 43008)) `
  -and (($subAfterQuit.code -eq 40300) -or ($subAfterQuit.code -eq 43009)) `
  -and ($hist.code -eq 0)
Set-Result "QUIT-THEN-DENY-WRITE" $quitOk @{
  quit = $quit.code; stamina = $quit.data.staminaRefunded; caps = $capQ
  msgCode = $msgAfterQuit.code; subCode = $subAfterQuit.code; hist = $hist.code
}

# --- second bounty: terminal 43008/43009 + cancel paths ---
$inv2 = Invoke-Api POST "/admin/invites" '{"count":2,"remark":"v1810-lc2"}' $ah
$d1 = [string](@($inv2.data.codes)[0]); $d2 = [string](@($inv2.data.codes)[1])
$s2 = Get-Random -Maximum 99999
$q1 = ("134{0:D8}" -f (Get-Random -Maximum 99999999))
$q2 = ("133{0:D8}" -f (Get-Random -Maximum 99999999))
Invoke-Api POST "/auth/sms/send" ("{`"phone`":`"$q1`",`"scene`":`"REGISTER`"}") | Out-Null
$regP2 = Invoke-Api POST "/auth/register" ((@{inviteCode=$d1;nickname=("Pub2"+$s2);phone=$q1;smsCode="123456";username=("pub2"+$s2);password="Test1234"} | ConvertTo-Json -Compress))
Invoke-Api POST "/auth/sms/send" ("{`"phone`":`"$q2`",`"scene`":`"REGISTER`"}") | Out-Null
$regC2 = Invoke-Api POST "/auth/register" ((@{inviteCode=$d2;nickname=("Clm2"+$s2);phone=$q2;smsCode="123456";username=("clm2"+$s2);password="Test1234"} | ConvertTo-Json -Compress))
$hP2 = @{ Authorization = ("Bearer " + $regP2.data.token) }
$hC2 = @{ Authorization = ("Bearer " + $regC2.data.token) }

# recreate body properly
$createBody2 = @{
  type = "RENT_SEEK"; title = ("LC2-" + $s2); difficulty = "NORMAL"; rewardAmount = 200; confirmLowReward = $true
  deadlineAt = $dl; taskTags = @("帮寻房")
  warrantFields = @{ district = "汇川"; rentBudgetMin = 1000; rentBudgetMax = 2000; layout = "两室"; expectMoveInDate = "2026-09-01"; acceptAgency = $false; extra = "qa" }
  checklistItemCodes = @("VERIFY_AUTHENTIC","SITE_VISIT_RECORD","PHOTO_EVIDENCE")
} | ConvertTo-Json -Compress -Depth 6
$cr2 = Invoke-Api POST "/bounties" $createBody2 $hP2
$bid2 = $cr2.data.id
Invoke-Api POST ("/admin/bounty-reviews/" + $bid2) '{"result":"APPROVE","reason":"ok"}' $ah | Out-Null
Invoke-Api POST ("/bounties/" + $bid2 + "/claims") "{}" $hC2 | Out-Null

# cancel WITHOUT submissions -> CANCELLED + full refund path
$accBefore = Invoke-Api GET "/wallet/account" "" $hP2
$cancelNo = Invoke-Api POST ("/bounties/" + $bid2 + "/cancel") '{"reason":"no-sub-cancel"}' $hP2
$detCancel = Invoke-Api GET ("/bounties/" + $bid2) "" $hP2
$accAfter = Invoke-Api GET "/wallet/account" "" $hP2
$msgTerm = Invoke-Api POST ("/bounties/" + $bid2 + "/messages") '{"content":"after-cancel"}' $hP2
$subTerm = Invoke-Api POST ("/bounties/" + $bid2 + "/submissions") $subBody $hC2
$histTerm = Invoke-Api GET ("/bounties/" + $bid2 + "/messages?page=1&pageSize=10") "" $hP2
$balBefore = [decimal]$accBefore.data.balance
$balAfter = [decimal]$accAfter.data.balance
$cancelNoOk = ($cancelNo.code -eq 0) -and ($detCancel.data.status -eq "CANCELLED") `
  -and ($msgTerm.code -eq 43008) -and (($subTerm.code -eq 43009) -or ($subTerm.code -eq 40300)) `
  -and ($histTerm.code -eq 0) `
  -and (-not [bool]$detCancel.data.capabilities.canSendMessage) `
  -and (-not [bool]$detCancel.data.capabilities.canSubmit) `
  -and ($balAfter -ge $balBefore)
Set-Result "CANCEL-NO-SUB-THEN-43008-09" $cancelNoOk @{
  cancel = $cancelNo.code; status = $detCancel.data.status
  msg = $msgTerm.code; sub = $subTerm.code; balBefore = $balBefore; balAfter = $balAfter
  caps = $detCancel.data.capabilities
}

# REJECTED path: create + reject
$createBody3 = @{
  type = "RENT_SEEK"; title = ("LC3-" + $s2); difficulty = "NORMAL"; rewardAmount = 200; confirmLowReward = $true
  deadlineAt = $dl; taskTags = @("帮寻房")
  warrantFields = @{ district = "汇川"; rentBudgetMin = 1000; rentBudgetMax = 2000; layout = "两室"; expectMoveInDate = "2026-09-01"; acceptAgency = $false; extra = "qa" }
  checklistItemCodes = @("VERIFY_AUTHENTIC","SITE_VISIT_RECORD","PHOTO_EVIDENCE")
} | ConvertTo-Json -Compress -Depth 6
$cr3 = Invoke-Api POST "/bounties" $createBody3 $hP2
$bid3 = $cr3.data.id
Invoke-Api POST ("/admin/bounty-reviews/" + $bid3) '{"result":"REJECT","reason":"reject-qa"}' $ah | Out-Null
$detR = Invoke-Api GET ("/bounties/" + $bid3) "" $hP2
$msgR = Invoke-Api POST ("/bounties/" + $bid3 + "/messages") '{"content":"rej"}' $hP2
$subR = Invoke-Api POST ("/bounties/" + $bid3 + "/submissions") $subBody $hC2
Set-Result "REJECTED-43008-09" (($detR.data.status -eq "REJECTED") -and ($msgR.code -eq 43008) -and (($subR.code -eq 43009) -or ($subR.code -eq 40300))) @{
  status = $detR.data.status; msg = $msgR.code; sub = $subR.code; caps = $detR.data.capabilities
}

# COMPLETED path: create, approve, claim, submit, approve submission, settle
$createBody4 = @{
  type = "RENT_SEEK"; title = ("LC4-" + $s2); difficulty = "NORMAL"; rewardAmount = 200; confirmLowReward = $true
  deadlineAt = $dl; taskTags = @("帮寻房")
  warrantFields = @{ district = "汇川"; rentBudgetMin = 1000; rentBudgetMax = 2000; layout = "两室"; expectMoveInDate = "2026-09-01"; acceptAgency = $false; extra = "qa" }
  checklistItemCodes = @("VERIFY_AUTHENTIC","SITE_VISIT_RECORD","PHOTO_EVIDENCE")
} | ConvertTo-Json -Compress -Depth 6
# new claimer for completed flow
$inv3 = Invoke-Api POST "/admin/invites" '{"count":1,"remark":"v1810-lc3"}' $ah
$e1 = [string](@($inv3.data.codes)[0])
$q3 = ("132{0:D8}" -f (Get-Random -Maximum 99999999))
Invoke-Api POST "/auth/sms/send" ("{`"phone`":`"$q3`",`"scene`":`"REGISTER`"}") | Out-Null
$regC3 = Invoke-Api POST "/auth/register" ((@{inviteCode=$e1;nickname=("Clm3"+$s2);phone=$q3;smsCode="123456";username=("clm3"+$s2);password="Test1234"} | ConvertTo-Json -Compress))
$hC3 = @{ Authorization = ("Bearer " + $regC3.data.token) }
$cr4 = Invoke-Api POST "/bounties" $createBody4 $hP2
$bid4 = $cr4.data.id
Invoke-Api POST ("/admin/bounty-reviews/" + $bid4) '{"result":"APPROVE","reason":"ok"}' $ah | Out-Null
Invoke-Api POST ("/bounties/" + $bid4 + "/claims") "{}" $hC3 | Out-Null
$sub4 = Invoke-Api POST ("/bounties/" + $bid4 + "/submissions") $subBody $hC3
$sid4 = $sub4.data.id
if (-not $sid4) { $sid4 = $sub4.data.submissionId }
$sr = Invoke-Api POST ("/admin/submission-reviews/" + $sid4) '{"result":"APPROVE","reason":"ok"}' $ah
$detSettle = Invoke-Api GET ("/bounties/" + $bid4) "" $hP2
$capSettle = $detSettle.data.capabilities
# settle: fee 20, give 180 to claimer
$uidC3 = $regC3.data.user.id
if (-not $uidC3) { $uidC3 = $regC3.data.userId }
if (-not $uidC3) {
  $meC = Invoke-Api GET "/user/me" "" $hC3
  $uidC3 = $meC.data.id
}
$settleBody = ("{`"items`":[{`"userId`":$uidC3,`"amount`":180,`"chivalryBonus`":0}]}")
$settle = Invoke-Api POST ("/bounties/" + $bid4 + "/settlement") $settleBody $hP2
$detDone = Invoke-Api GET ("/bounties/" + $bid4) "" $hP2
$msgDone = Invoke-Api POST ("/bounties/" + $bid4 + "/messages") '{"content":"after-complete"}' $hP2
$subDone = Invoke-Api POST ("/bounties/" + $bid4 + "/submissions") $subBody $hC3
$histDone = Invoke-Api GET ("/bounties/" + $bid4 + "/messages?page=1&pageSize=10") "" $hP2
$doneOk = ($settle.code -eq 0) -and ($detDone.data.status -eq "COMPLETED") `
  -and ($msgDone.code -eq 43008) -and (($subDone.code -eq 43009) -or ($subDone.code -eq 40300)) `
  -and ($histDone.code -eq 0) -and ([bool]$detDone.data.capabilities.canReadMessages)
Set-Result "COMPLETED-43008-09-READONLY" $doneOk @{
  sr = $sr.code; settle = $settle.code; status = $detDone.data.status
  msg = $msgDone.code; sub = $subDone.code; caps = $detDone.data.capabilities; preCaps = $capSettle
}

# --- cancel WITH approved submissions should enter allocation, NOT full cancel refund ---
$inv4 = Invoke-Api POST "/admin/invites" '{"count":1,"remark":"v1810-lc4"}' $ah
$f1 = [string](@($inv4.data.codes)[0])
$q4 = ("131{0:D8}" -f (Get-Random -Maximum 99999999))
Invoke-Api POST "/auth/sms/send" ("{`"phone`":`"$q4`",`"scene`":`"REGISTER`"}") | Out-Null
$regC4 = Invoke-Api POST "/auth/register" ((@{inviteCode=$f1;nickname=("Clm4"+$s2);phone=$q4;smsCode="123456";username=("clm4"+$s2);password="Test1234"} | ConvertTo-Json -Compress))
$hC4 = @{ Authorization = ("Bearer " + $regC4.data.token) }
$createBody5 = @{
  type = "RENT_SEEK"; title = ("LC5-" + $s2); difficulty = "NORMAL"; rewardAmount = 200; confirmLowReward = $true
  deadlineAt = $dl; taskTags = @("帮寻房")
  warrantFields = @{ district = "汇川"; rentBudgetMin = 1000; rentBudgetMax = 2000; layout = "两室"; expectMoveInDate = "2026-09-01"; acceptAgency = $false; extra = "qa" }
  checklistItemCodes = @("VERIFY_AUTHENTIC","SITE_VISIT_RECORD","PHOTO_EVIDENCE")
} | ConvertTo-Json -Compress -Depth 6
$cr5 = Invoke-Api POST "/bounties" $createBody5 $hP2
$bid5 = $cr5.data.id
Invoke-Api POST ("/admin/bounty-reviews/" + $bid5) '{"result":"APPROVE","reason":"ok"}' $ah | Out-Null
Invoke-Api POST ("/bounties/" + $bid5 + "/claims") "{}" $hC4 | Out-Null
$sub5 = Invoke-Api POST ("/bounties/" + $bid5 + "/submissions") $subBody $hC4
$sid5 = $sub5.data.id
if (-not $sid5) { $sid5 = $sub5.data.submissionId }
Invoke-Api POST ("/admin/submission-reviews/" + $sid5) '{"result":"APPROVE","reason":"ok"}' $ah | Out-Null
$balB5 = [decimal]((Invoke-Api GET "/wallet/account" "" $hP2).data.balance)
$cancelYes = Invoke-Api POST ("/bounties/" + $bid5 + "/cancel") '{"reason":"has-sub-cancel"}' $hP2
$det5 = Invoke-Api GET ("/bounties/" + $bid5) "" $hP2
$balA5 = [decimal]((Invoke-Api GET "/wallet/account" "" $hP2).data.balance)
$msg5 = Invoke-Api POST ("/bounties/" + $bid5 + "/messages") '{"content":"x"}' $hP2
# Expect per §6.23/§9.14: enter allocation (PENDING_SETTLE or keep frozen), NOT CANCELLED+full refund
$expectAlloc = ($det5.data.status -eq "PENDING_SETTLE") -or (($cancelYes.data.status -eq "PENDING_SETTLE"))
$wrongFullRefund = ($det5.data.status -eq "CANCELLED") -and ($balA5 -gt $balB5)
$cancelWithSubOk = $expectAlloc -and (-not $wrongFullRefund) -and (($msg5.code -eq 43008) -or ($det5.data.status -eq "PENDING_SETTLE"))
Set-Result "CANCEL-WITH-SUB-TO-ALLOC" $cancelWithSubOk @{
  cancel = $cancelYes.code; status = $det5.data.status; balBefore = $balB5; balAfter = $balA5
  msg = $msg5.code; note = "expect PENDING_SETTLE/alloc; CANCELLED+refund is Fail"
}

# PENDING_SETTLE cancel allowed: force settle path then... actually need PENDING_SETTLE status
# If cancel-with-sub failed and went CANCELLED, create PENDING_SETTLE via settle preview path:
# Approve submission then manually check if status becomes PENDING_SETTLE on review
# ReviewService may set PENDING_SETTLE on submission approve - check
$createBody6 = @{
  type = "RENT_SEEK"; title = ("LC6-" + $s2); difficulty = "NORMAL"; rewardAmount = 200; confirmLowReward = $true
  deadlineAt = $dl; taskTags = @("帮寻房")
  warrantFields = @{ district = "汇川"; rentBudgetMin = 1000; rentBudgetMax = 2000; layout = "两室"; expectMoveInDate = "2026-09-01"; acceptAgency = $false; extra = "qa" }
  checklistItemCodes = @("VERIFY_AUTHENTIC","SITE_VISIT_RECORD","PHOTO_EVIDENCE")
} | ConvertTo-Json -Compress -Depth 6
$inv5 = Invoke-Api POST "/admin/invites" '{"count":1,"remark":"v1810-lc5"}' $ah
$g1 = [string](@($inv5.data.codes)[0])
$q5 = ("130{0:D8}" -f (Get-Random -Maximum 99999999))
Invoke-Api POST "/auth/sms/send" ("{`"phone`":`"$q5`",`"scene`":`"REGISTER`"}") | Out-Null
$regC5 = Invoke-Api POST "/auth/register" ((@{inviteCode=$g1;nickname=("Clm5"+$s2);phone=$q5;smsCode="123456";username=("clm5"+$s2);password="Test1234"} | ConvertTo-Json -Compress))
$hC5 = @{ Authorization = ("Bearer " + $regC5.data.token) }
$cr6 = Invoke-Api POST "/bounties" $createBody6 $hP2
$bid6 = $cr6.data.id
Invoke-Api POST ("/admin/bounty-reviews/" + $bid6) '{"result":"APPROVE","reason":"ok"}' $ah | Out-Null
Invoke-Api POST ("/bounties/" + $bid6 + "/claims") "{}" $hC5 | Out-Null
$sub6 = Invoke-Api POST ("/bounties/" + $bid6 + "/submissions") $subBody $hC5
$sid6 = $sub6.data.id; if (-not $sid6) { $sid6 = $sub6.data.submissionId }
Invoke-Api POST ("/admin/submission-reviews/" + $sid6) '{"result":"APPROVE","reason":"ok"}' $ah | Out-Null
$det6 = Invoke-Api GET ("/bounties/" + $bid6) "" $hP2
$psCancel = $false
if ($det6.data.status -eq "PENDING_SETTLE" -or $det6.data.status -eq "IN_COLLAB") {
  $cap6 = $det6.data.capabilities
  $canCancelPs = [bool]$cap6.canCancel
  $cancelPs = Invoke-Api POST ("/bounties/" + $bid6 + "/cancel") '{"reason":"ps-cancel"}' $hP2
  $det6b = Invoke-Api GET ("/bounties/" + $bid6) "" $hP2
  $msg6 = Invoke-Api POST ("/bounties/" + $bid6 + "/messages") '{"content":"x"}' $hP2
  $psCancel = $canCancelPs -and ($cancelPs.code -eq 0) -and ($msg6.code -eq 43008)
  Set-Result "PENDING_SETTLE-OR-COLLAB-CANCEL" $psCancel @{
    before = $det6.data.status; after = $det6b.data.status; canCancel = $canCancelPs
    cancel = $cancelPs.code; msg = $msg6.code; capsBefore = $cap6
  }
} else {
  Set-Result "PENDING_SETTLE-OR-COLLAB-CANCEL" $false @{ before = $det6.data.status; note = "unexpected status after submission approve" }
}

# three-way: api field list vs FE type vs sample response
$sampleKeys = @($capP.PSObject.Properties.Name)
$feInterface = @("canCancel","canSendMessage","canReadMessages","canViewSubmissions","canSubmit","canSettle","canQuitClaim","canRepublish","canDispute")
$triOk = ($missing.Count -eq 0) -and (@($feInterface | Where-Object { $_ -notin $sampleKeys }).Count -eq 0)
Set-Result "TRIAD-CAPABILITIES" $triOk @{ apiSample = $sampleKeys; fe = $feInterface }

$results["meta"] = @{ ts = (Get-Date).ToString("s"); bid = $bid; bid2 = $bid2; bid4 = $bid4; bid5 = $bid5; bid6 = $bid6 }
$json = $results | ConvertTo-Json -Depth 8
[IO.File]::WriteAllText((Join-Path $outDir "v1810_results.json"), $json, [Text.Encoding]::UTF8)
$lines = New-Object System.Collections.Generic.List[string]
foreach ($k in $results.Keys) {
  if ($k -eq "meta") { continue }
  [void]$lines.Add(($k + "=" + $(if ($results[$k].pass) { "PASS" } else { "FAIL" })))
}
[IO.File]::WriteAllText((Join-Path $outDir "v1810_summary.txt"), ($lines -join "`r`n"), [Text.Encoding]::UTF8)
Write-Host "V1810 DONE"
