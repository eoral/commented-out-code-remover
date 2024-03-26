Let's assume that we have a code base that includes lots of commented out code blocks, and we want to remove those blocks. This is an attempt to automate this task.

It is not easy to determine if a code block is commented out or not. So, I decided to use SonarQube because it can already detect commented out code blocks.

**Install latest SonarQube server:**
```
docker pull sonarqube
```

**Start SonarQube server on port 9001 on your machine:**
```
docker run -d --name sonarqube-on-port-9001 -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9001:9000 sonarqube:latest
```

**Wait for 1 minute. Then, open `http://localhost:9001` in your browser. Wait until you see the login page.**

**Change default password for SonarQube server (default username: admin, default password: admin, new password: admin12345):**
```
curl -v -u admin:admin -X POST "http://localhost:9001/api/users/change_password?login=admin&previousPassword=admin&password=admin12345"
```

**Create a user token for SonarQube server (copy token from response and save it somewhere)**
```
curl -v -u admin:admin12345 -X POST "http://localhost:9001/api/user_tokens/generate?name=my-user-token"
```

**Here is an example of token response:**
```
{
	"login": "admin",
	"name": "my-user-token",
	"token": "squ_fe5cfbdcede08fd6302f4e24b6a43d90ae419880",
	"createdAt": "2023-09-30T18:32:33+0000",
	"type": "USER_TOKEN"
}
```

**Install SonarScanner CLI. This is the tool that will scan our code and push the findings to SonarQube server.**
```
docker pull sonarsource/sonar-scanner-cli
```

**Start code scan. Replace 'my-sonar-token' and 'path-of-the-code-base-in-my-machine' with their actual values.**
```
docker run --rm --net=host -e SONAR_HOST_URL="http://localhost:9001" -e SONAR_SCANNER_OPTS="-Dsonar.projectKey=my-project" -e SONAR_TOKEN="my-sonar-token" -v "path-of-the-code-base-in-my-machine:/usr/src" sonarsource/sonar-scanner-cli
```

Code scan may take some time. For one of my code bases which has 30K lines of code, it took 23 minutes. It was slower than I expected. I think, SonarScanner CLI is a generic scanner (works for all languages). On SonarQube's website, I found this statement: 'The SonarScanner CLI is the scanner to use when there is no specific scanner for your build system.' Maybe that's why it is slow.

When code scan is completed, findings are pushed to SonarQube server. Findings are the issues found in the code base. When you log in to SonarQube server, you should see a project named 'my-project'. When you go to the project page and list the issues, you should see issues with message 'Remove this commented out code'. These are the issues we are interested in. The JSON structure of these issues is as follows (other fields removed for clarity):
```
{
	"rule": "typescript:S125",
	"component": "my-project:src/file.ts",
	"project": "my-project",
	"textRange": {
		"startLine": 35,
		"endLine": 35,
		"startOffset": 8,
		"endOffset": 76
	},
	"status": "OPEN",
	"message": "Remove this commented out code."
}
```

- **rule:** It is the type of the issue, which states that sections of code should not be commented out. Its value varies depending on the language of your code base. For example, it can be `typescript:S125`, `java:S125` or `python:S125`. But, it always ends with `:S125`.
- **component:** It is the project key concatenated with `:` and path of a file that contains commented out code blocks.
- **project:** It is the project key.
- **textRange:** It specifies the start and end positions of each commented out code block. When we delete all the chars within this range, the issue will be fixed.
- **status:** It specifies the status of the issue. Possible values are `OPEN`, `CONFIRMED`, `REOPENED`, `RESOLVED`, `CLOSED`. But, in our case, it will always be `OPEN`.

As you see, each issue tells us:
- the path of the file
- the start and end positions of the commented out code block to be deleted

**This Maven project is prepared to process those issues programmatically. It basically does the following:**
- It connects to SonarQube server.
- It fetches all issues.
- It selects issues with rule `xxx:S125` and status `OPEN`.
- It deletes commented out code blocks by using the issues.

**How to run this Maven project:**
- Clone this repository to your machine. Let's assume you cloned it into directory `/usr`.
- Open `/usr/commented-out-code-remover/src/main/resources/app.properties` file and edit each property.
  - Leave `sonarQubeBaseUrl` as is if you didn't change SonarQube server port.
  - Leave `projectKey` as is if you didn't change project key.
  - Set the path of the code base in your machine to `projectDirectory`. If your os is Windows, use `\\` as path delimiter.
  - Set the user token that we created for SonarQube server to `sonarQubeToken`.
  - Leave `charsetOfCodeFiles` as is if all the files in your code base are UTF-8 encoded.
- Open a terminal window and go to `/usr/commented-out-code-remover`. Run `mvn clean package`.
- In the terminal window, go to `/usr/commented-out-code-remover/target`. Run `java -classpath commented-out-code-remover-0.1.jar com.eoral.commentedoutcoderemover.App`.

**Notes:**
- Please keep in mind that SonarQube scanner may give false positives. In other words, it can detect a normal comment block as a commented out code block. So, after deleting commented out code blocks, check the files that were changed before pushing the changes to your VCS.
