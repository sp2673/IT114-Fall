<table><tr><td> <em>Assignment: </em> It114 Milestone1</td></tr>
<tr><td> <em>Student: </em> Shrey Patel (sp2673)</td></tr>
<tr><td> <em>Generated: </em> 10/24/2022 11:42:34 PM</td></tr>
<tr><td> <em>Grading Link: </em> <a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-007-F22/it114-milestone1/grade/sp2673" target="_blank">Grading</a></td></tr></table>
<table><tr><td> <em>Instructions: </em> <ol><li>Create a new branch called Milestone1</li><li>At the root of your repository create a folder called Project</li><li>Create a milestone1.md file inside the project folder</li><li>Git add/commit/push it to Github</li><li>Create a pull request from Milestone1 to main (don't complete/merge it yet)</li><li>Copy in the latest Socket sample code from the most recent Socket Part example of the lessons</li><ol><li>Recommended Part 5, but Part 4 should be sufficient</li><li><a href="https://github.com/MattToegel/IT114/tree/Module5/Module5">https://github.com/MattToegel/IT114/tree/Module5/Module5</a>&nbsp;<br></li></ol><li>Git add/commit the baseline</li><li>Ensure the sample is working and fill in the below deliverables</li><li>Get the markdown content or the file and paste it into the milestone1.md file or replace the file with the downloaded version</li><li>Git add/commit/push all changes</li><li>Complete the pull request merge from step 5</li><li>Locally checkout main</li><li>git pull origin main</li></ol></td></tr></table>
<table><tr><td> <em>Deliverable 1: </em> Startup </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="http://via.placeholder.com/400x120/009955/fff?text=Complete"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot showing your server being started and running</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106455418/197671942-fdaa6694-9553-4b1e-9227-82c2cc20fff5.png"/></td></tr>
<tr><td> <em>Caption:</em> <p>server is listening to port 3000 screenshot<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add screenshot showing your client being started and running</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106455418/197672483-d137f7ea-3f14-4ffb-b74c-e1cdd4641be0.png"/></td></tr>
<tr><td> <em>Caption:</em> <p>client is connected to server<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain the connection process</td></tr>
<tr><td> <em>Response:</em> <p>server is listening to port 3000 by using /connect localhost:3000 and you can<br>connect client to the server<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 2: </em> Sending/Receiving </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="http://via.placeholder.com/400x120/009955/fff?text=Complete"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot(s) showing evidence related to the checklist</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106455418/197675595-6b6c89f9-7a3f-421c-9335-0cd8cd498236.png"/></td></tr>
<tr><td> <em>Caption:</em> <p>server shows its having a conversation<br></p>
</td></tr>
<tr><td><img width="768px" src="https://user-images.githubusercontent.com/106455418/197675626-801537a7-60a8-44e9-b166-9d897b3b3c81.png"/></td></tr>
<tr><td> <em>Caption:</em> <p>2 client talking to each other<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain how the messages are sent, broadcasted, and received</td></tr>
<tr><td> <em>Response:</em> <p>server thread is used to send messages and connection status for the server.<br>room helps send messages and connection status. creates room and joins rooms using<br>rooms the client create a different to chat in. by using server thread<br>clients can send and receive messages.<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 3: </em> Disconnecting / Terminating </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="http://via.placeholder.com/400x120/009955/fff?text=Complete"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot(s) showing evidence related to the checklist on the right</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://user-images.githubusercontent.com/106455418/197676736-760708dd-fee5-4f32-aaa9-fc899395eaa6.png"/></td></tr>
<tr><td> <em>Caption:</em> <p>sam used /disconnect and server was still running <br></p>
</td></tr>
<tr><td><img width="768px" src="https://user-images.githubusercontent.com/106455418/197676811-fa032634-8096-4057-88f2-55a3846c223a.png"/></td></tr>
<tr><td> <em>Caption:</em> <p>sam terminated and it shows his disconnected but the server is still running<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain how the various disconnects/terminations are handled</td></tr>
<tr><td> <em>Response:</em> <p>in room it checks for connection status each time the message is passed.<br>the server is still runs when client is disconnected. server then delete the<br>clients name after .<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 4: </em> Misc </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="http://via.placeholder.com/400x120/009955/fff?text=Complete"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add the pull request for this branch</td></tr>
<tr><td> <a rel="noreferrer noopener" target="_blank" href="https://github.com/sp2673/IT114-Fall/pull/2">https://github.com/sp2673/IT114-Fall/pull/2</a> </td></tr>
</table></td></tr>
<table><tr><td><em>Grading Link: </em><a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-007-F22/it114-milestone1/grade/sp2673" target="_blank">Grading</a></td></tr></table>