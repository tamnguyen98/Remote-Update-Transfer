# Remote-Update-Transfer
## Goal:
Given the following situation: Let say you have a application that has a large directory (> 100 GB) and it receieve frequent updates that are several gigabytes, but you have a data cap with your ISP (e.g XFinity). How do you update the applications on multiple devices WITHOUT transfer the whole directory into an external drive every time there's an update?

Well that's what this application is made for. It scans through a old copy of the application and keeps track of everyfile's last modify date and then compare it a recently update directory and see what was changed (update) in the application's directory. From there it copies ONLY the changes to the destination.

## Benifit of this:
- Reduce transfer time.
- Reduce read/write weardown of a storage device
- (Should be) universal since it's written in Java

## Instructions to wrong
### Windows:
1. Run cmd as Admin
2. navigate to the jar file (a copy of the jar file is in the bin folder)
3. run ```java -jar exec.jar```
4. follow the on screen instructions

### Linux environment
1. Open Terminal and navigate to the jar file
2. execute the jar file with sudo ```sudo java -jar exec.jar```
