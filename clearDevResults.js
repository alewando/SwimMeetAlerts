// Deletes all results
db.swimmers.update({},{$unset:{results:1}},false,true);
// Clears all meet completed dates
db.meeturls.update({},{$unset:{lastCompleted:1}},false,true);
