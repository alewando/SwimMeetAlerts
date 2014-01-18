function initPage() {
	// 'Add swimmer' listener
	$('#addButton').click(function() { 
		$('#addSwimmer').toggle(300);
		$('#firstName').focus();
	});

	// 'Unfollow' listener
	$('.unfollow').click(unfollowSwimmer);
	
	// Load followed swimmers
	getSwimmers();
}


function getSwimmers() {
	jsRoutes.controllers.Application.getFollowedSwimmers().ajax({
		success: updateSwimmerData
	});
}

function updateSwimmerData() {
	
}

function unfollowSwimmer() {
	jsRoutes.controllers.ManageSwimmers.unfollow().ajax({
		type: "POST",
		data: JSON.stringify({ swimmerId : $(this).attr('data-id')}),
		dataType: "json",
		contentType: "application/json",
		success: function(resp) {
			alert('yay');
		}	 		
	});
//	$.ajax({
//		url: "routes.ManageSwimmers.unfollow",
//		type: "POST",
//		data: JSON.stringify({ swimmerId : $(this).attr('data-id')}),
//		dataType: "json",
//		contentType: "application/json",
//		success: function(resp) {
//			alert('yay');
//		}	 
//	}
}