<?php

class Event
{
    protected $id;
    protected $type;
    protected $data;
    protected $time;
    protected $retry;
    protected $comment;
   
    /**
     * Event constructor.
     * @param array $event [id=>id,type=>type,data=>data,retry=>retry,comment=>comment]
     */
    public function __construct(array $event)
    {
        $this->id = isset($event['id']) ? $event['id'] : null;
        $this->type = isset($event['type']) ? $event['type'] : null;
        $this->data = isset($event['data']) ? $event['data'] : null;
        $this->time = isset($event['time']) ? $event['time'] : null;
        $this->retry = isset($event['retry']) ? $event['retry'] : null;
        $this->comment = isset($event['comment']) ? $event['comment'] : null;
    }
    
    public function __toString()
    {
        $event = [];
        
        strlen($this->id) > 0 AND $event[] = sprintf('id:%s', $this->id);
        strlen($this->data) > 0 AND $event[] = sprintf('data:%s', $this->data);
        strlen($this->type) > 0 AND $event[] = sprintf('event:%s', $this->type);
        strlen($this->time) > 0 AND $event[] = sprintf('time:%s', $this->time);
        strlen($this->comment) > 0 AND $event[] = sprintf('comment:%s', $this->comment);//:comments
        strlen($this->retry) > 0 AND $event[] = sprintf('retry:%s', $this->retry);//millisecond
        
        return implode(";", $event) . "\n";
    }
}

header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Content-Type: text/event-stream');
header('Cache-Control: no-cache');
header('Connection: keep-alive');
header('X-Accel-Buffering: no'); 

error_reporting(E_ALL);

$fromList = $_GET['source'];
$toList   = $_GET['target'];

if(!isset($_GET['source']) || $_GET['source'] == '' || !isset($_GET['target']) || $_GET['target'] == ''){
    $event = [
        'id'    => "sourceDestinationNotSet",
        'data'  => (string) $fromList
	];
	echo new Event($event);
	//ob_end_flush();
	flush();
    exit;
}

//echo json_encode("FromList: " . $fromList);
//echo json_encode("ToList: " . $toList);

//Monterrey
//$fromList = str_replace('/opt/pacex/repository/raster/', '/share/RASTER/', $fromList);
//$toList = str_replace('/opt/pacex/repository/raster/', '/share/RASTER/', $toList);
//$toList = str_replace('/opt/pacex/repository/press/', '/share/RASTER/', $toList);

//Kentucky
//$fromList = str_replace('/opt/pacex/repository/raster/', '/share/pacex/raster/', $fromList);
//$toList = str_replace('/opt/pacex/repository/raster/', '/share/pacex/raster/', $toList);
//$toList = str_replace('/opt/pacex/repository/press/', '/share/pacex/raster/', $toList);

//France
$fromList = str_replace('/opt/esprint/repository/raster/', '/share/FujiRIP/', $fromList);
$toList = str_replace('/opt/pacex/repository/raster/', '/share/FujiRIP/', $toList);
$toList = str_replace('/opt/pacex/repository/press/', '/share/pacex/press/', $toList);

//Tunis
//$fromList = str_replace('/opt/esprint/repository/raster/', '/share/FujiRIP/', $fromList);
//$toList = str_replace('/opt/pacex/repository/raster/', '/share/FujiRIP/', $toList);
//$toList = str_replace('/opt/pacex/repository/press', '/share/pacex/press', $toList);

$fromListArray = explode(" ", $fromList);
$toListArray = explode(" ", $toList);

//echo json_encode("fromListArray: " . $fromListArray);
//echo json_encode("toListArray: " . $toListArray);

$lengthFrom = count($fromListArray);
$lengthTo = count($toListArray);
set_time_limit(0); 
if($lengthFrom <= 0 || $lengthTo <= 0 || $lengthFrom != $lengthTo){
    $event = [
        'id'    => "sourceDestinationInvalid",
        'data'  => (string) $lengthFrom
	];
	echo new Event($event);
	//ob_end_flush();
	flush(); 
    exit;
}

$buffer = 1024 * 1024 * 80;
if ($_SERVER['REQUEST_METHOD'] == "GET") {
	for ($i = 0; $i < $lengthFrom; $i++) {
		$ret = 0;
		$time = time();
		$from = $fromListArray[$i];
		$to = $toListArray[$i];
		if($from == '' || $from == ' ' || $from == null){
		    continue;
		}
		if(!file_exists($from)){
		    $event = [
		        'id'    => "sourceFileNotFound",
		        'data'  => (string) $from
			];
			echo new Event($event);
			//ob_end_flush();
			//flush(); 
		    continue;
		}
	
		//echo json_encode("From: " . $from);
	
		$size = filesize($from);
		//$time = time();
		
		$fin = fopen($from, "rb");
		if(!$fin){
			fclose($fin);
			$event = [
		        'id'    => "sourceFileCannotOpen",
		        'data'  => (string) $from
			];
			echo new Event($event);
			//ob_end_flush();
			//flush(); 
		    continue;
		}
		
		$parentFolder = dirname($to);
		mkdir($parentFolder, 0777, true);
		chmod($parentFolder, 0777);
		//echo json_encode("To: " . $to);
		
		$fout = fopen($to, "w");
		if(!$fout){
			fclose($fout);
			$event = [
		        'id'    => "destinationFileCannotOpen",
		        'data'  => (string) $to
			];
			echo new Event($event);
			//ob_end_flush();
			flush(); 
		    continue;
		}
	
		while(!feof($fin)) {
		    $ret += fwrite($fout, fread($fin, $buffer));
		}
		
		$file = basename($to);
		if($ret != $size){
			$event = [
		        'id'    => "CopyCorrupt",
		        'data'  => (string) $file
			];
			echo new Event($event);
		}
		$time = time() - $time;
		$event = [
		   'id'    => "block",
		    'data'  => (string)($ret),
		    'comment' => (string) $file,
		    'time'  => (string) ($time)
		];
		echo new Event($event);
		
		flush();
		//ob_end_flush();
		fclose($fin);
		fclose($fout);
	}
}
$event = [
   'id'    => "CLOSE",
   'data'  => "SUCCESS"
];
    
echo new Event($event);
//ob_end_flush();
flush(); 
sleep(1);
exit;
?>