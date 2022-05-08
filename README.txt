How to run our application?

Before running the application make sure you update the configuration file accordingly.
The configuration file is named config.txt.
In the first row enter the credentials path, and in the second row enter your bucket name.
For the excecution run the following command from the jar's directory:
java -jar Text-Analysis-in-the-Cloud.jar InputFile OutPutFile n [terminate]

How our application works?

The implemantation contains 3 modules that comunicate with each other by sqs. (as described in the assigment's specification)

The local application workflow:
1. Uploading the input file to s3 (to the bucket named in the configuration file).
2. Checking if the manager is working, and if not, creating it.
3. Sending the location of the input file to the manager in SQS "getAppMessagesSQS" and saving the message id for later use.
4. Waiting for a message from the manager in SQS "sendAppMessagesSQS" containing the previous message id and changing the visibility timeout to be 1 second.
5. Saving the ID-INFO.json from {bucketname}\{messageId} key prefix. 
    This json file contains a list of links, parsing types an outputs from the workers. Those matching the input file data.
6. Creating an HTML containing the relevant data according to the ID-INFO.json file.
7. Optional - If got a termination argument, sending terminate message in SQS "getAppMessagesSQS" to the manager.
8. Exit.

The manager workflow:
1. Creates the 4 SQS's for communication with application and the workers (will be described next).
2. Waits for messages from the local applications on  SQS "getAppMessagesSQS".
3. When Getting a txt file message from the local application: 
    Splitting the input file and sending the messages for the workers in SQS "sendWorkersMessagesSQS" (message for each line).
    Creating the ID-INFO.json that will contain the results data.
    Creating additional workers if needed, according to the amount of messages in SQS "sendWorkersMessagesSQS".
4. Waits for messages from the workers on SQS "getWorkersMessagesSQS".
5. When getting a message from a worker:
5.1. Updating the relevant ID-INFO.json file according to the id mentioned in the message.
5.2. Checking if the ID-INFO.json file is complete. If so, sending the path to the application as a SQS message in SQS "sendAppMessagesSQS".
6. When getting a "terminate" message from the application, the manager raises a termination flag an waits for all the opened application requests to be finished.
    Later, closing all the SQSs, closing all the workers instances and finally closing itself.

The worker workflow:
1. The worker waits for messages from the manager in SQS "sendWorkersMessagesSQS" and changing it's visibility timeout to be 30 minutes.
2. Downloding the file link needed to be analyzed as txt file and parses it according to the analysing type in th message.
3. Uploading it to s3 in {bucketname}\{appMessageId}\{currMessageId}.txt.
4. Sending to the manager the result path\error message if an error occured in SQS "getWorkersMessagesSQS".
5. Deleting the input file and the result file from the local memory.

SQS's architecture:

1. getAppMessagesSQS - For sending messages from the local applications to the manager.
Messages format: {link-for-s3/terminate (for termination)}
2. sendAppMessagesSQS - For sending the results from the manager to the applications.
Messages format: {appMessageId}
3. sendWorkerMessagesSQS - For sending messages from the manager to the workers.
Messages format: {appMessageId}|{parsingType}|{link}
4. getWorkerMessagesSQS - For sending messages from the workers to the manager.
Messages format: {appMessageId}|{parsingType}|{inputLink}|{errorMessage/outputLink}

Additional info:

ami: ami-0b36cd6786bcfe120
Instance type: T2_MICRO

Performances -
Total running time: 1054 seconds
The n we used: 1


Regarding security - the credentials are being read from the LocalApplication's .aws directory and insterted directly into the UserData when creating the manager.

Regarding scalablity - the "heavy lifting" is done by the workers which are created by the manager according to the amount of job requests.
The manager itself is built from 2 "listeners" threads that take care of the SQS communications. In addition the manager holds a thread pool which is responsible for handling the local application's requests and for updating the ID-INFO.json with the responses from the workers.
The way that the manager was built allows it to be duplicated as many times as needed which helps to make the whole system more scalable.
There is one downside in the way we built our system in regards to scalability - there is only a single SQS for all the LocalApplication's results, so they are all waiting on the same SQS.
(If we had more time we would've changed this implementation to include a unique SQS for each application but we realized this scalibility issue only in the last day).

Persistance - Each worker when it recieves a message, it changes its VisibilityTimeout to 30 minutes, so if it dies in the middle of the parsing, the message can be parsed later on by another worker. The manager makes sure after each request to open the right amount of workers needed according to the number of tasks in the SQS, meaning that even if a worker dies it will be replaced in the handling on the next request.
