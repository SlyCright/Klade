# GitHub Runner Setup Notes (Windows Server)

## The Problem (Why This Note Exists)
When running `./config.cmd` during GitHub self-hosted runner setup, the interactive registration 
process **hangs/freezes** after displaying "Runner Registration".  

## The Solution 
Use **non-interactive mode** to skip all prompts:
```powershell
.\config.cmd --url https://github.com/SlyCright/Klade --token TOKEN_HERE --unattended --replace 
--runasservice 