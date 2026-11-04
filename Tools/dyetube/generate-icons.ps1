# DyeTube: generates launcher icons from Tools/dyetube/icon-source.png (the full icon, white background)
# and Tools/dyetube/logo-mark.png (only the gradient triangle, transparent).
#
#   mipmap-*/ic_launcher_foreground.png   adaptive foreground (Android 8+): the full icon in the 72 dp visible zone
#   mipmap-*/ic_launcher_monochrome.png   themed icon (Android 13+): the triangle shape only
#   mipmap-*/ic_launcher.png              legacy icon (Android 6-7), rounded square
#   drawable-nodpi/dye_logo_mark.png      the triangle for the splash screen
#
# Usage (from repo root):  powershell -ExecutionPolicy Bypass -File Tools/dyetube/generate-icons.ps1

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$root = Resolve-Path (Join-Path $PSScriptRoot '..\..')
$res = Join-Path $root 'app\src\main\res'
$source = [System.Drawing.Image]::FromFile((Join-Path $PSScriptRoot 'icon-source.png'))
$mark = [System.Drawing.Image]::FromFile((Join-Path $PSScriptRoot 'logo-mark.png'))

$densities = [ordered]@{ mdpi = 1.0; hdpi = 1.5; xhdpi = 2.0; xxhdpi = 3.0; xxxhdpi = 4.0 }

function New-Canvas([int]$size) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)
    @($bmp, $g)
}

function Save-Png($bmp, [string]$dir, [string]$name) {
    $folder = Join-Path $res $dir
    New-Item -ItemType Directory -Force $folder | Out-Null
    $bmp.Save((Join-Path $folder $name), [System.Drawing.Imaging.ImageFormat]::Png)
}

foreach ($d in $densities.Keys) {
    $scale = $densities[$d]
    $dir = "mipmap-$d"

    # Adaptive foreground: 108 dp layer, the full icon in the central 72 dp (white edges meet the white background)
    $layer = [int](108 * $scale); $inner = [int](72 * $scale); $off = [int](($layer - $inner) / 2)
    $c = New-Canvas $layer; $bmp = $c[0]; $g = $c[1]
    $g.DrawImage($source, $off, $off, $inner, $inner)
    $g.Dispose(); Save-Png $bmp $dir 'ic_launcher_foreground.png'; $bmp.Dispose()

    # Monochrome: the triangle as large as it is in the full icon (about 43 % of its height)
    $c = New-Canvas $layer; $bmp = $c[0]; $g = $c[1]
    $h = $inner * 0.43; $w = $h * $mark.Width / $mark.Height
    $g.DrawImage($mark, [float]($layer / 2 - $w / 2 + $inner * 0.02), [float]($layer / 2 - $h / 2), [float]$w, [float]$h)
    $g.Dispose(); Save-Png $bmp $dir 'ic_launcher_monochrome.png'; $bmp.Dispose()

    # Legacy: 48 dp rounded square
    $size = [int](48 * $scale)
    $c = New-Canvas $size; $bmp = $c[0]; $g = $c[1]
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $r = $size * 0.2; $dd = $r * 2
    $path.AddArc(0, 0, $dd, $dd, 180, 90); $path.AddArc($size - $dd, 0, $dd, $dd, 270, 90)
    $path.AddArc($size - $dd, $size - $dd, $dd, $dd, 0, 90); $path.AddArc(0, $size - $dd, $dd, $dd, 90, 90)
    $path.CloseFigure()
    $art = New-Object System.Drawing.Bitmap $size, $size
    $ag = [System.Drawing.Graphics]::FromImage($art)
    $ag.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $ag.DrawImage($source, 0, 0, $size, $size); $ag.Dispose()
    $brush = New-Object System.Drawing.TextureBrush $art
    $g.FillPath($brush, $path)
    $brush.Dispose(); $art.Dispose(); $g.Dispose()
    Save-Png $bmp $dir 'ic_launcher.png'; $bmp.Dispose()
}

# Splash triangle: 96 dp tall at xxxhdpi
$h = 96 * 4; $w = [int]($h * $mark.Width / $mark.Height)
$c = New-Canvas ([Math]::Max($w, $h)); $bmp = $c[0]; $g = $c[1]
$g.DrawImage($mark, [float](($bmp.Width - $w) / 2), 0, [float]$w, [float]$h)
$g.Dispose(); Save-Png $bmp 'drawable-nodpi' 'dye_logo_mark.png'; $bmp.Dispose()

$source.Dispose(); $mark.Dispose()
Write-Host 'Icons written'
