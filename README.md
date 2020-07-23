# Remote-Update-Transfer
## Goal:
Given the following situation: Let say you have an application that has a large directory (> 100 GB), and it receives frequent updates that are several gigabytes, but you have a data cap with your ISP (e.g., Xfinity). How do you update the applications on multiple devices WITHOUT transfer the whole directory into an external drive every time there's an update?

Well, that's what this application is made for. It scans through an old copy of the application, keeps track of every file's last modified date and then compares it to a recently updated directory and see what was changed (update) in the application's directory. From there, it copies ONLY the changes to the destination.

## Benefits of this:
- Reduce transfer time.
- Reduce read/write wear down of a storage device
- (Should be) universal since it's written in Java

## Instructions to wrong
### Windows:
1. Run cmd as Admin
2. navigate to the jar file (a copy of the jar file is in the bin folder)
3. run ```java -jar exec.jar```
4. follow the on-screen instructions

### Linux environment
1. Open Terminal and navigate to the jar file
2. execute the jar file with Sudo ```sudo Java -jar exec.jar```
