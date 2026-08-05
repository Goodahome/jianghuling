# QA requirements section 9.5 (v1.7) acceptance - ASCII script
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
function Invite-Admin($token) {
  $inv=Invoke-Api POST '/admin/invites' '{"quota":5,"remark":"v17"}' $token
  $c=$null
  if ($inv.data.codes) { $c=@($inv.data.codes)[0] }
  elseif ($inv.data.code) { $c=$inv.data.code }
  return $c
}

# admin login
$adm=Invoke-Api POST '/admin/auth/login' '{"username":"admin","password":"admin123"}'
$at=$adm.data.token
Pass 'ADM' ($adm.code -eq 0) @{ code=$adm.code }

$suffix=Get-Random -Min 10000 -Max 99999
$codeA=Invite-Admin $at
$phoneA=('137{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$userA="v17a$suffix"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneA`",`"scene`":`"REGISTER`"}" | Out-Null
$regA=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$codeA`",`"phone`":`"$phoneA`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userA`",`"nickname`":`"Inv$suffix`"}"
$tokenA=$regA.data.token
$idA=$regA.data.user.id
$accA=Invoke-Api GET '/wallet/account' $null $tokenA
$ledA=Invoke-Api GET '/wallet/ledgers?page=1&pageSize=20' $null $tokenA
$typesA=@($ledA.data.list | ForEach-Object { $_.type })
$grantA=@($ledA.data.list | Where-Object { $_.type -eq 'REGISTER_GRANT' } | Select-Object -First 1)
Pass 'V17-01-REGISTER-GRANT' (($regA.code -eq 0) -and ([decimal]$accA.data.balance -eq 500) -and ($typesA -contains 'REGISTER_GRANT') -and ([decimal]$grantA.amount -eq 500)) @{
  bal=$accA.data.balance; types=$typesA; grantAmt=$grantA.amount; grantBiz=$grantA.bizNo; code=$regA.code
}

# flags default off
Pass 'V17-03-FLAGS' (($accA.data.rechargeEnabled -eq $false) -and ($accA.data.withdrawEnabled -eq $false)) @{
  recharge=$accA.data.rechargeEnabled; withdraw=$accA.data.withdrawEnabled
}

$rc=Invoke-Api POST '/wallet/recharge' '{"amount":10,"clientRequestId":"v17-rc-1"}' $tokenA
$wd=Invoke-Api POST '/wallet/withdraw' '{"amount":10,"clientRequestId":"v17-wd-1"}' $tokenA
Pass 'V17-03-API-CLOSED' (($rc.code -eq 42004) -and ($wd.code -eq 42004)) @{ rc=$rc.code; wd=$wd.code; rcMsg=$rc.message; wdMsg=$wd.message }
Pass 'V17-04-NOT-DELETED' (($rc.code -eq 42004) -and ($wd.code -eq 42004) -and ($rc.code -ne 40400) -and ($rc.code -ne 50000)) @{ note='endpoints exist, return 42004' }

# meta wallet features
$feat=Invoke-Api GET '/meta/wallet-features'
Pass 'V17-04-META' (($feat.code -eq 0) -and ($feat.data.rechargeEnabled -eq $false) -and ($feat.data.withdrawEnabled -eq $false) -and ([decimal]$feat.data.registerGrantAmount -eq 500) -and ([decimal]$feat.data.inviteRewardAmount -eq 100)) @{
  data=$feat.data; code=$feat.code
}

# A creates user invite, B registers
$uInv=Invoke-Api POST '/user/invites' '{}' $tokenA
$uCode=$uInv.data.code
if (-not $uCode) { $uCode=$uInv.data.inviteCode }
# list if needed
if (-not $uCode) {
  $ul=Invoke-Api GET '/user/invites' $null $tokenA
  $uCode=@($ul.data.list | Where-Object { $_.status -eq 'ACTIVE' } | Select-Object -First 1).code
}
$phoneB=('138{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$userB="v17b$suffix"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneB`",`"scene`":`"REGISTER`"}" | Out-Null
$regB=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$uCode`",`"phone`":`"$phoneB`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userB`",`"nickname`":`"Bee$suffix`"}"
$tokenB=$regB.data.token
$idB=$regB.data.user.id
$accB=Invoke-Api GET '/wallet/account' $null $tokenB
$accA2=Invoke-Api GET '/wallet/account' $null $tokenA
$ledA2=Invoke-Api GET '/wallet/ledgers?page=1&pageSize=30' $null $tokenA
$invReward=@($ledA2.data.list | Where-Object { $_.type -eq 'INVITE_REWARD' })
Pass 'V17-01-B-GRANT' (($regB.code -eq 0) -and ([decimal]$accB.data.balance -eq 500)) @{ balB=$accB.data.balance; regB=$regB.code; uCode=$uCode; uInv=$uInv.code }
Pass 'V17-02-INVITE-REWARD' (($invReward.Count -ge 1) -and ([decimal]$accA2.data.balance -eq 600) -and ([decimal]$invReward[0].amount -eq 100)) @{
  balA=$accA2.data.balance; rewardCnt=$invReward.Count; rewardAmt=$invReward[0].amount; rewardBiz=$invReward[0].bizNo; uInvMsg=$uInv.message
}

# invite reward once: try second invitee
$uInv2=Invoke-Api POST '/user/invites' '{}' $tokenA
$uCode2=$uInv2.data.code
if (-not $uCode2) {
  $ul2=Invoke-Api GET '/user/invites' $null $tokenA
  $uCode2=@($ul2.data.list | Where-Object { $_.status -eq 'ACTIVE' -and $_.code -ne $uCode } | Select-Object -First 1).code
}
$phoneC=('139{0:D8}' -f (Get-Random -Min 0 -Max 99999999))
$userC="v17c$suffix"
Invoke-Api POST '/auth/sms/send' "{`"phone`":`"$phoneC`",`"scene`":`"REGISTER`"}" | Out-Null
$regC=Invoke-Api POST '/auth/register' "{`"inviteCode`":`"$uCode2`",`"phone`":`"$phoneC`",`"smsCode`":`"123456`",`"password`":`"Test1234`",`"username`":`"$userC`",`"nickname`":`"Cee$suffix`"}"
$accA3=Invoke-Api GET '/wallet/account' $null $tokenA
$ledA3=Invoke-Api GET '/wallet/ledgers?page=1&pageSize=50' $null $tokenA
$invRewardAll=@($ledA3.data.list | Where-Object { $_.type -eq 'INVITE_REWARD' })
# A should get another +100 for second invitee (once per invitee, not once ever)
Pass 'V17-02-ONCE-PER-INVITEE' (($regC.code -eq 0) -and ($invRewardAll.Count -eq 2) -and ([decimal]$accA3.data.balance -eq 700)) @{
  balA=$accA3.data.balance; rewardCnt=$invRewardAll.Count; regC=$regC.code; note='100 per invitee once'
}

# admin adjust
$adj=Invoke-Api POST ("/admin/users/"+$idA+"/assets/adjust") '{"delta":50,"reason":"qa v17 grant","assetType":"BALANCE"}' $at
if ($adj.code -ne 0) {
  $adj=Invoke-Api POST ("/admin/users/"+$idA+"/assets/adjust") '{"delta":50,"remark":"qa v17","type":"BALANCE"}' $at
}
$accA4=Invoke-Api GET '/wallet/account' $null $tokenA
Pass 'V17-05-ADMIN-ADJUST' (($adj.code -eq 0) -and ([decimal]$accA4.data.balance -ge 750)) @{
  adj=$adj.code; adjMsg=$adj.message; bal=$accA4.data.balance
}

# messages unread
$uc=Invoke-Api GET '/messages/unread-count' $null $tokenB
$msgs=Invoke-Api GET '/messages?page=1&pageSize=20' $null $tokenB
$unreadItems=@($msgs.data.list | Where-Object { $_.read -eq $false -or $_.readFlag -eq $false -or $_.read -eq 0 })
$countBefore=[int]$uc.data.count
$mid=$null
if ($msgs.data.list -and $msgs.data.list.Count -gt 0) { $mid=$msgs.data.list[0].id }
$read=@{ code=-1 }
if ($mid) { $read=Invoke-Api POST ("/messages/"+$mid+"/read") '{}' $tokenB }
$uc2=Invoke-Api GET '/messages/unread-count' $null $tokenB
$countAfter=[int]$uc2.data.count
Pass 'V17-06-UNREAD' (($uc.code -eq 0) -and ($countBefore -ge 1) -and ($msgs.code -eq 0) -and ($read.code -eq 0) -and ($countAfter -lt $countBefore)) @{
  before=$countBefore; after=$countAfter; mid=$mid; read=$read.code; listCnt=@($msgs.data.list).Count
}

# frontend static checks
$walletVue=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\WalletView.vue')
$msgVue=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\views\hero\MessagesView.vue')
$layout=[IO.File]::ReadAllText('f:\Jinanghu_Ling\frontend\src\layouts\HeroLayout.vue')
$hideUi=(($walletVue -match 'canRecharge') -and ($walletVue -match 'v-if="showOps"') -and ($walletVue -match 'rechargeEnabled'))
$badge=(($layout -match 'unreadBadge') -and ($layout -match 'unreadCount'))
$unreadStyle=(($msgVue -match 'class="\{ unread') -or ($msgVue -match "unread: !m.read") -or ($msgVue -match 'unread-title'))
Pass 'V17-03-FE-HIDE' $hideUi @{ hideUi=$hideUi }
Pass 'V17-06-FE-BADGE' ($badge -and $unreadStyle) @{ badge=$badge; unreadStyle=$unreadStyle }

$R['meta']=@{ idA=$idA; idB=$idB; suffix=$suffix; ts=(Get-Date).ToString('s') }
($R|ConvertTo-Json -Depth 8)|Set-Content "$outDir\v17_results.json" -Encoding UTF8
$lines=@(); foreach($k in $R.Keys){ if($k -eq 'meta'){continue}; $lines += ("{0}={1}" -f $k,$(if($R[$k].pass){'PASS'}else{'FAIL'})) }
$lines|Set-Content "$outDir\v17_summary.txt"
Write-Host 'V17 DONE'
$lines
