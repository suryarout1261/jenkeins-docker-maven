@echo off
echo 🚀 Deploying via Jenkins...

git add .
git commit -m "auto deploy"
git push

echo ✅ Done! Jenkins pipeline triggered.