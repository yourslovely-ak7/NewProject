async function getUserDetails()
{
			const response= await fetch('/NewProject/userdetails',{
				method: 'GET',
			})
			
			if(response.ok)
			{
				const data= await response.json();
				const parentDiv= document.getElementById("userDetails");
				parentDiv.innerHTML= `
									<h1>Welcome, ${data.name}</h1>
									<p>First Name: ${data.firstName}</p>
									<p>Last Name: ${data.lastName}</p>
									<p>Email Address: ${data.email}</p>
									<p>Gender : ${data.gender}</p>
									`
			}
}

async function logoutUser()
{
	const response= await fetch('/NewProject/userdetails',{
					method: 'POST',
				})
				
				if(response.ok)
				{
					window.location.href= '/NewProject/login.html';
				}
}

async function triggerRefresh()
{
			const response= await fetch('/NewProject/refresh',{
				method: 'POST',
			})
			
			if(response.ok)
			{
				const data= await response.json();
				alert(`${data.message}`);
			}
}

async function tryAccess()
{
			const response= await fetch('/NewProject/access',{
				method: 'GET',
			})
			
			const data= await response.json();
			if(response.ok)
			{
				alert(`${data.message}`);
			}
			else
			{
				alert(`${data.error}`);
			}
}

window.onload= getUserDetails();