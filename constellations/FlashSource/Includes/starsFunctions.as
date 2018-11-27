/*
 * Create stars according to its constellation
 */
 
#include "NetServices.as"

//Global variables
var curMsgStar:Number = 0;
var curMsgConstellation:Number = 0;
var linesCreated:Array = new Array();
var totalCreated:Number = 0;
var lastCall:Date;
var recomboGateway:String = new String("http://creta.cesar.org.br/recombo/gateway")
var servidor = _root.servidor;
if (servidor != undefined) {
	recomboGateway = _level0.servidor + "/gateway";
}
var maxWidth:Number = 800;
var maxHeight:Number = 600;
//End Global variables

NetServices.setDefaultGatewayUrl(recomboGateway); 

function createSky(constellations:Array):Void{
	this.createEmptyMovieClip("sky", this.getNextHighestDepth());
	//Create every constellation with its stars
	for (var i:Number = 0; i < constellations.length; i++){
		for (var j:Number = 0; j < constellations[i].stars.length; j++){	
			var star_mc = sky.attachMovie("star", "mc_star_"+i+"_"+j, sky.getNextHighestDepth());
			var current = this.sky["mc_star_"+i+"_"+j]
			current.obj = constellations[i].stars[j];
			current._x = current.obj.x;
			current._y = current.obj.y;	
			current._visible = false;
			totalCreated++;
			
			/*
			 * ROLL OVER
			 * shows constellation and message
			 */
			current.onRollOver = current.onReleaseOutside = function(){
				showConstellation(this);
				showMessage(this);
			}
			/*
			 * ROLL OUT
			 * hide constellation and message
			 */
			current.onRollOut = current.onReleaseOutside = function(){
				hideConstellation(this);
			}			
		}
	}
	getAllMessages();
}//End createSky

/*
 * function assignMessage
 * assigns a message to a star and call a function to control alpha according to its date. 
 * alpha is controlled here because it only works for visible stars
 */
function assignMessage(msg:String):Void{
	//If there´s a message, assign to star
	if (!(msg.length <=0)){
		//Star in wich message will be assigned
		//Test with the current constellation number and current star number
		var newStar:MovieClip = sky["mc_star_"+curMsgConstellation+"_"+curMsgStar];
		//Is the refered star don't exist, it's because the numbers of stars
		//on the refered constellation is over
		if (newStar == undefined){
			//Go to the first star in the next constellation
			curMsgStar = 0;
			curMsgConstellation++;
			newStar = sky["mc_star_"+curMsgConstellation+"_"+curMsgStar];
	
			if (newStar == undefined){
				//If next constellation don´t exist is because we are on the last star
				//So let´s start to light them again
				curMsgConstellation = 0;
				curMsgStar = 0;
				newStar = sky["mc_star_"+curMsgConstellation+"_"+curMsgStar];
			}
		}
		curMsgStar++;
		
		newStar._visible = true;
		newStar.obj.message = msg;
		newStar.gotoAndPlay(2);

		setStarSize(newStar, msg.length);
		setStarAlpha(newStar);		
	};
}//End assignMessage

/*
 * function setStarSize
 * set the star size according to a message length
 */
function setStarSize(star:MovieClip, msgSize:Number):Void{
	//Working with stars size
	var starSize:Number = msgSize / 8;
	
	//Minimum size
	if (starSize < 3){
		starSize = 3;
	};
	//Maximum size
	if (starSize > 6){
		starSize = 6;
	}
	
	//Setting stars size
	star._width = starSize;
	star._height = starSize;

}//End setStarSize

/*
 * function setStarAlpha
 * set the star alpha according to its born time
 */
function setStarAlpha(star:MovieClip):Void{
	
	//Crate and alpha scale to light the old stars
	var alphaScale:Number = 100/totalCreated;
	
	for (var n:Number=0; n < constellations.length; n++){
		for (var m:Number = 0; m < constellations[n].stars.length; m++){
			oldStar = sky["mc_star_"+n+"_"+m];
			if (oldStar._visible){
				//Ligth the stars
				oldStar._alpha -= alphaScale;

				//Minimum alpha
				if (oldStar._alpha < 6 ){
					oldStar._alpha = 6;
				}
			}
		}
	}
	
	star._alpha = 100;
}//End setStarAlpha

/*
 * function showConstellation
 * show the constellation to which the star belong
 */
function showConstellation(star:MovieClip):Void{
	//Get star constellation
	var starNum:String = star._name.substr(8,star._name.length);
	var char:String = new String("");
	var cont:Number = 0;	
	while (char != "_"){
		char = starNum.substr(cont,1);
		cont++;
	}	
	var thisContellation:String = new String(starNum.substr(0,cont-1));

	//Start find stars sisters
	var starSky:MovieClip = star._parent;
	var thisStar:Number = 0;
	
	var starSister:MovieClip = starSky["mc_star_"+thisContellation+"_"+thisStar]

	while (starSister != undefined && starSister._visible){
		thisStar++
		var nextSister:MovieClip = starSky["mc_star_"+thisContellation+"_"+thisStar]

		if (nextSister != undefined && nextSister._visible){
			starSky.createEmptyMovieClip("line"+thisStar, starSky.getNextHighestDepth());
			with (starSky["line"+thisStar] ){
				lineStyle( 1, 0xFFFFFF, 100 );
				moveTo( starSister._x, starSister._y );
				lineTo( nextSister._x, nextSister._y ); 
			}	
			linesCreated.push("line"+thisStar)
			
		}
		starSister = nextSister;
	}

}//End showConstellation

/*
 * function showMessage
 * show the message assigned to the star
 */
function showMessage(star:MovieClip):Void{

	var starSky:MovieClip = star._parent;

	star.msgKepper = star._parent.createEmptyMovieClip("msgKepper"+starSky.getNextHighestDepth(), starSky.getNextHighestDepth());
	star.msgKepper.attachMovie("ballon","ballon_mc",star.msgKepper.getNextHighestDepth());
	star.msgKepper.ballon_mc.text_txt.selectable = false;
	star.msgKepper.ballon_mc.text_txt.text = star.obj.message;

	//Sizing the ballon according to the message
	star.msgKepper.ballon_mc.fill_mc._height = star.msgKepper.ballon_mc.text_txt.textHeight + 20;
	star.msgKepper.ballon_mc.fill_mc._width = star.msgKepper.ballon_mc.text_txt.textWidth + 10;

	star.msgKepper._x = star._x
	star.msgKepper._y = star._y	

	while ((star.msgKepper._x + star.msgKepper._width) > maxWidth){
		star.msgKepper._x -= 1;
	}
	
	while ((star.msgKepper._y + star.msgKepper._height) > maxHeight){
		star.msgKepper._y -= 1;
	}
	
	while ((star.msgKepper._x) < 0){
		star.msgKepper._x += 1;
	}
	
	while ((star.msgKepper._y) < 70){
		star.msgKepper._y += 1;
	}		
}//End showMessage

/*
 * function hideConstellation
 * hides the constellation to which the star belong
 */
function hideConstellation(star:MovieClip):Void{
	var starSky:MovieClip = star._parent;
	
	for (var k:Number = 0; k< linesCreated.length; k++){
		removeMovieClip(starSky[linesCreated[k]])				
	}
	linesCreated.splice(0,linesCreated.length)
	removeMovieClip(star.msgKepper);
}//End hideConstellation

/*
 * function getAllMessages
 * gets all sent messages
 */
function getAllMessages():Void{
	var msgs:Array = new Array();
	var gatewayConnection = NetServices.createGatewayConnection();
	var service = gatewayConnection.getService("Constellations", this);
	service.findAllSMSMessages();

	findAllSMSMessages_Result = function (obj) {
		getNewDate();
		msgs = obj;
		for (var s:Number = 0; s < msgs.length; s++){
			assignMessage(msgs[s].message);
		};		
	}
}

/*
 * function getMessagesByDate
 * gets all sent messages starting from a certain date
 */
function getMessagesByDate(owner):Void{
	var msgs:Array = new Array();
	var gatewayConnection = NetServices.createGatewayConnection();
	var service = gatewayConnection.getService("Constellations", owner);
	service.findAllSMSMessagesFromArrivalDate(lastCall);
}

/*
 * function getNewDate
 * gets the server date
 */
function getNewDate():Void{
	var gatewayConnection = NetServices.createGatewayConnection();
	var service = gatewayConnection.getService("Constellations", this);
	service.getNewDate();

	getNewDate_Result = function (obj) {
		lastCall = obj;
	}
}