param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("CLEAN1", "CLEAN2")]
    [string]$Label
)

# Backward-compatible entry point. The full runner resolves IDs from the
# current canonical seed and response data instead of historical UUIDs.
& (Join-Path $PSScriptRoot 'e2e-full-regression.ps1') -Label $Label
exit $LASTEXITCODE
