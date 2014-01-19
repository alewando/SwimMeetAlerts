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
		success: updateSwimmersData
	});
}

function updateSwimmersData(swimmersData) {
	$('#watchedSwimmers').empty();
	$.each(swimmersData, function(idx, swimmerData) {
		updateSwimmerData(swimmerData);
	});
}

function updateSwimmerData(swimmerData) {
	var html = swimmerTemplate(swimmerData);
	// TODO: find/replace existing swimmer div if exists instead of clearing all
	$('#watchedSwimmers').append(html);
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