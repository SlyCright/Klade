# GitHub Runner Setup Notes (Windows Server)

## Problem 1: Interactive Registration Hangs

When running `./config.cmd` during GitHub self-hosted runner setup, the interactive registration process hangs or freezes after displaying "Runner Registration".

### Solution

Use non-interactive mode to skip all prompts. Run `config.cmd` with the following parameters:
- `--url`: Your GitHub repository URL
- `--token`: Your registration token
- `--unattended`: Skips interactive prompts
- `--replace`: Replaces any existing runner with the same name
- `--runasservice`: Configures the runner to run as a Windows service

`.\config.cmd --url https://github.com/SlyCright/Klade --token TOKEN_HERE --unattended --replace 
--runasservice`

---

## Problem 2: Deployment Permissions Denied

After setup, the runner (running as NT AUTHORITY\NETWORK SERVICE) cannot stop the KladeApp Windows service (running as LocalSystem), causing file-in-use errors during deployment.

### Solution Options

#### Option A: Run Runner as LocalSystem (Recommended for This Project)

**Rationale**: This approach simplifies permissions for a single-maintainer hobby project while accepting the security trade-off of elevated runner privileges.

**Steps**:
1. Open the Services management console (`services.msc`)
2. Locate the GitHub Actions Runner service (name format: "GitHub Actions Runner (MACHINE_NAME)")
3. Right-click the service and select **Properties**
4. Navigate to the **Log On** tab
5. Select **"Local System account"**
6. Check **"Allow service to interact with desktop"**
7. Apply the changes and restart the service

#### Option B: Grant Minimal Permissions (More Secure Alternative)

If keeping NETWORK SERVICE, grant specific service control rights to allow the runner to stop/start the KladeApp service. This involves modifying the service's security descriptor using `sc.exe` with the `sdset` parameter to add the necessary access control entries for the NETWORK SERVICE account.

`sc.exe sdset KladeApp "D:(A;;CCLCSWRPLORC;;;IU)(A;;CCLCSWRPWPDTLOCRRC;;;SY)(A;;CCLCSWRPWPDTLOCRRC;;;BA)(A;;RP;;;NS)"`

