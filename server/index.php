<?php
require_once 'config.php';

$regid = isset($_GET['id']) ? $_GET['id'] : '';
if (isset($_GET['privacy'])) {
  readfile('../datenschutz.html');
  exit;
}
if (isset($_GET['contact'])) {
  readfile('../impressum.html');
  exit;
}

function showMessage($msg) {
  echo "<div class=\"card-panel\">
    <span class=\"blue-text text-darken-2\">$msg</span>
  </div>";
}
?>

<!DOCTYPE html>
<html>
    <head>
        <title>Cloud Alarm</title>

        <!-- Compiled and minified CSS -->
        <link rel="stylesheet" href="css/materialize.min.css">
        <link rel="stylesheet" href="fonts/icons.css">
        <link rel="stylesheet" href="css/style.css">

        <!--Let browser know website is optimized for mobile-->
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

        <!-- Favicon, created by realfavicongenerator.net -->
        <link rel="icon" type="image/png" sizes="32x32" href="favicon-32x32.png">
        <link rel="icon" type="image/png" sizes="16x16" href="favicon-16x16.png">
        <meta name="theme-color" content="#2b99f2">
    </head>
    <body>
        <main>          
          <nav class="blue" role="navigation">
            <div class="nav-wrapper container">
                <a target="_blank" id="logo-container" href="https://play.google.com/store/apps/details?id=net.kollnig.alarm" class="center brand-logo">Cloud Alarm</a>
            </div>
          </nav>
          <div class="section no-pad-bot" id="index-banner">
            <div class="container">
              <div class="row center">
<?php
    if (!($regid)):
      showMessage('Please, generate a link in the app first. Find it <a target="_blank" href="https://play.google.com/store/apps/details?id=net.kollnig.alarm">here</a> on Google Play.');
    else:
      if ($_SERVER['REQUEST_METHOD'] != 'POST' || !isset($_POST['alarmtime'])): 
?>
                  <form class="col s12" action="<?php echo basename(__FILE__); ?>?id=<?php echo htmlspecialchars($regid); ?>" method="post">                 
                      <div class="row">
                        <div class="input-field col s12">
                            <input type="time" class="timepicker" name="alarmtime">
                            <label for="q">Type alarm time:</label>
                            <!--<p>Select time: <input type="time" name="alarmtime"></p>
                            <p><input type="submit" /></p>-->
                        </div>
                      </div>

                      <button class="btn waves-effect waves-light blue" type="submit" name="action">Set alarm
                        <i class="material-icons right">send</i>
                      </button>
                                                   
                  </form>
<?php
    else:
              $alarm = explode(':', $_POST['alarmtime']);
              if (count($alarm) != 2) {
                showMessage('Invalid alarm time.');
              } else {
                // Payload data you want to send to Android device(s)
                // (it will be accessible via intent extras)    
                $data = array('hours' => $alarm[0], 'minutes' => $alarm[1]);

                // Send push notification
                $response = json_decode(sendPushNotification($data, $regid), true);

                if ($response['failure'] == 0) {
                    showMessage('Alarm should be set. Check on your device.');
                } else {
                    showMessage('Setting of alarm failed.');
                }
              }
    endif;
  endif;
?>    
              </div>
            </div>
          </div>
        </main>
        <footer class="page-footer blue">
            <div class="footer-copyright">
              <div class="container">
                © 2020 Konrad Kollnig
                <span class="right"><a class="grey-text text-lighten-3" href="?contact">Impressum</a></span><br>
                <span class="right"><a class="grey-text text-lighten-3" href="?privacy">Dateschutzerklärung</a></span>
              </div>
            </div>
        </footer>
        <!--JavaScript at end of body for optimized loading-->
        <script type="text/javascript" src="js/jquery-2.1.1.min.js"></script>
        <script type="text/javascript" src="js/materialize.min.js"></script>
        <script type="text/javascript">            
            $(document).ready(function() {
                $('.timepicker').pickatime({
                  default: 'now', // Set default time: 'now', '1:30AM', '16:30'
                  fromnow: 0,       // set default time to * milliseconds from now (using with default = 'now')
                  twelvehour: false, // Use AM/PM or 24-hour format
                  donetext: 'OK', // text for done-button
                  cleartext: 'Clear', // text for clear-button
                  canceltext: 'Cancel', // Text for cancel-button
                  autoclose: false, // automatic close timepicker
                  ampmclickable: true, // make AM PM clickable
                  aftershow: function(){} //Function for after opening timepicker
                });
            });
        </script>
    </body>
</html>

<?php
function sendPushNotification($data, $id) {
    $apiKey  =         SECRET_KEY;
    $post    = array(  'registration_ids'  => array($id),
                       'data'              => $data             );
    $headers = array(  'Authorization: key=' . $apiKey,
                       'Content-Type: application/json'         );

    // Issue request to FCM
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'https://fcm.googleapis.com/fcm/send');
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($post));
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers); 
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true); // Suppress printing result

    $result = curl_exec($ch);
    if (curl_errno($ch)) die('FCM error: ' . curl_error($ch));
    curl_close($ch);

    // Debug FCM response
    return $result;
}