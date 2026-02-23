# Kill any Python processes, wait, then run fast fix script
Get-Process python* -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 3
Write-Host "Running PMU fix (fast)..."
& python -u C:\AppDev\Apps\git-fast\watch\tools\pmu_fix_fast.py COM3
