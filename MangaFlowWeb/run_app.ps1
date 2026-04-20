$jarPath = "C:\Users\win  10\Desktop\HSF302_Project\HSF302_PROJECT\MangaFlowWeb\target\MangaFlowWeb-0.0.1-SNAPSHOT.jar"
$logFile = "C:\Users\win  10\Desktop\HSF302_Project\HSF302_PROJECT\MangaFlowWeb\app.log"

# Start the application and redirect output to log file
& java -jar $jarPath > $logFile 2>&1

# Keep the window open
Read-Host "Application finished. Press Enter to close."

