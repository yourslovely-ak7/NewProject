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

window.onload= getUserDetails();