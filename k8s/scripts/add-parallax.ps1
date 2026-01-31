# Script to add Parallax Mapping to Node Material JSON
# Adds ViewDirection block, parallaxScale input, and connects to PerturbNormalBlock

$InputFile = "C:\Users\beatk\Downloads\nodeMaterial (70).json"
$OutputFile = "C:\Users\beatk\Downloads\nodeMaterial-parallax.json"

Write-Host "Loading Node Material JSON..."
$json = Get-Content $InputFile -Raw | ConvertFrom-Json

# Find the blocks array
$blocks = $json.blocks

# Find highest block ID
$maxId = 0
foreach ($block in $blocks) {
    if ($block.id -gt $maxId) {
        $maxId = $block.id
    }
}
Write-Host "Highest block ID: $maxId"

# New block IDs
$viewDirectionBlockId = $maxId + 1
$parallaxScaleBlockId = $maxId + 2

Write-Host "Adding ViewDirection block with ID: $viewDirectionBlockId"
Write-Host "Adding ParallaxScale block with ID: $parallaxScaleBlockId"

# Create ViewDirection block
# WorldPos TransformBlock = 1754, cameraPosition InputBlock = 1815
$viewDirectionBlock = @{
    "customType" = "BABYLON.ViewDirectionBlock"
    "id" = $viewDirectionBlockId
    "name" = "ViewDirection"
    "comments" = "For Parallax Mapping"
    "visibleInInspector" = $false
    "visibleOnFrame" = $false
    "target" = 2
    "inputs" = @(
        @{
            "name" = "worldPosition"
            "inputName" = "worldPosition"
            "targetBlockId" = 1754
            "targetConnectionName" = "xyz"
            "isExposedOnFrame" = $true
            "exposedPortPosition" = -1
        },
        @{
            "name" = "cameraPosition"
            "inputName" = "cameraPosition"
            "targetBlockId" = 1815
            "targetConnectionName" = "output"
            "isExposedOnFrame" = $true
            "exposedPortPosition" = -1
        }
    )
    "outputs" = @(
        @{
            "name" = "output"
        },
        @{
            "name" = "xyz"
        },
        @{
            "name" = "x"
        },
        @{
            "name" = "y"
        },
        @{
            "name" = "z"
        }
    )
}

# Create ParallaxScale input block
$parallaxScaleBlock = @{
    "customType" = "BABYLON.InputBlock"
    "id" = $parallaxScaleBlockId
    "name" = "parallaxScale"
    "comments" = "Parallax depth scale"
    "visibleInInspector" = $true
    "visibleOnFrame" = $false
    "target" = 1
    "inputs" = @()
    "outputs" = @(
        @{
            "name" = "output"
        }
    )
    "type" = 1
    "mode" = 0
    "animationType" = 0
    "min" = 0
    "max" = 0
    "isBoolean" = $false
    "matrixMode" = 0
    "isConstant" = $false
    "groupInInspector" = ""
    "convertToGammaSpace" = $false
    "convertToLinearSpace" = $false
    "valueType" = "number"
    "value" = 0.05
}

# Add new blocks to the array
$blocks += $viewDirectionBlock
$blocks += $parallaxScaleBlock

# Find PerturbNormalBlock (ID 1761) and update it
for ($i = 0; $i -lt $blocks.Count; $i++) {
    if ($blocks[$i].id -eq 1761) {
        Write-Host "Found PerturbNormalBlock, updating connections..."

        # Update useParallaxOcclusion
        $blocks[$i].useParallaxOcclusion = $true

        # Update inputs
        for ($j = 0; $j -lt $blocks[$i].inputs.Count; $j++) {
            $inputName = $blocks[$i].inputs[$j].name

            if ($inputName -eq "viewDirection") {
                Write-Host "  Connecting viewDirection..."
                $blocks[$i].inputs[$j] = @{
                    "name" = "viewDirection"
                    "inputName" = "viewDirection"
                    "targetBlockId" = $viewDirectionBlockId
                    "targetConnectionName" = "output"
                    "isExposedOnFrame" = $true
                    "exposedPortPosition" = -1
                }
            }
            elseif ($inputName -eq "parallaxScale") {
                Write-Host "  Connecting parallaxScale..."
                $blocks[$i].inputs[$j] = @{
                    "name" = "parallaxScale"
                    "inputName" = "parallaxScale"
                    "targetBlockId" = $parallaxScaleBlockId
                    "targetConnectionName" = "output"
                    "isExposedOnFrame" = $true
                    "exposedPortPosition" = -1
                }
            }
            elseif ($inputName -eq "parallaxHeight") {
                Write-Host "  Connecting parallaxHeight to Ground upper texture (r channel)..."
                $blocks[$i].inputs[$j] = @{
                    "name" = "parallaxHeight"
                    "inputName" = "parallaxHeight"
                    "targetBlockId" = 1827
                    "targetConnectionName" = "r"
                    "isExposedOnFrame" = $true
                    "exposedPortPosition" = -1
                }
            }
        }
        break
    }
}

# Update blocks array
$json.blocks = $blocks

# Add editor locations for new blocks
$newLocations = @(
    @{
        "blockId" = $viewDirectionBlockId
        "x" = 400
        "y" = 400
        "isCollapsed" = $false
    },
    @{
        "blockId" = $parallaxScaleBlockId
        "x" = 400
        "y" = 500
        "isCollapsed" = $false
    }
)

$json.editorData.locations += $newLocations

Write-Host "Saving modified Node Material to: $OutputFile"
$json | ConvertTo-Json -Depth 100 | Set-Content $OutputFile -Encoding UTF8

Write-Host ""
Write-Host "Done! Parallax Mapping added." -ForegroundColor Green
Write-Host "Output file: $OutputFile"
Write-Host ""
Write-Host "To use:"
Write-Host "1. Open https://nme.babylonjs.com/"
Write-Host "2. Load $OutputFile"
Write-Host "3. Verify connections and adjust parallaxScale value (default 0.05)"
Write-Host "4. Export and replace original material"
