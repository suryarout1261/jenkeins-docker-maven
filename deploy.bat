@echo off
echo 🚀 Deploying...

git add .

git diff --cached --quiet
if %errorlevel%==0 (
    echo ⚠ No changes to commit → skipping push
    exit /b
)

git commit -m "auto deploy"
git push

echo ✅ Pipeline triggered