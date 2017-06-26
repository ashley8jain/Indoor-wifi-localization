package umd.mindlab.main;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer;
import com.esri.arcgisruntime.layers.ArcGISSublayer;
import com.esri.arcgisruntime.layers.FeatureCollectionLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.layers.SublayerList;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.LayerList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.portal.PortalUser;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.UserCredential;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorAttribute;
import com.esri.arcgisruntime.tasks.geocode.LocatorInfo;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class map extends AppCompatActivity {

    Context context;
    private MapView mMapView;
    //EditText location;
    ImageButton search,gpsbutton,wifibutton;
    Callout mCallout;
    LocatorTask locatorTask;
    GeocodeParameters mGeocodeParameters;
    GeocodeResult[] mGeocodedLocation = {null};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        context = this;
        mMapView = (MapView) findViewById(R.id.mapView);
//        search = (ImageButton) findViewById(R.id.search);
//        gpsbutton = (ImageButton) findViewById(R.id.gpsbutton);
        wifibutton = (ImageButton) findViewById(R.id.wifiB);

        // Set the DefaultAuthenticationChallegeHandler to allow authentication with the portal.
        DefaultAuthenticationChallengeHandler handler = new DefaultAuthenticationChallengeHandler(this);
        AuthenticationManager.setAuthenticationChallengeHandler(handler);

        // create a new Portal object
        final Portal portal = new Portal("http://uofmd.maps.arcgis.com/",true);
        // create a Map which is loaded from a webmap
        UserCredential creds = new UserCredential("maracai77", "locus4160");
        portal.setCredential(creds);
        portal.addDoneLoadingListener(new Runnable(){
            @Override
            public void run() {
                if (portal.getLoadStatus() == LoadStatus.LOADED) {
                    PortalUser user = portal.getUser();
                    Log.v("Map","user: "+user.getFullName());
                    Log.v("Map","portal loaded done");
                    Log.v("Map","portal status: "+portal.getLoadStatus());
                }
            }
        });
//        portal.loadAsync();


        // create a PortalItem based on a pre-defined portal id
        final PortalItem portalItem = new PortalItem(portal, "30010341c0d3439ab3337cf79d70dc6b");
        portalItem.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.v("Mapp","here: "+portalItem.getOwner());
                Log.v("Map","portalitem loaded done");
                Log.v("Map","portalitem status: "+portalItem.getLoadStatus());
                Log.v("Map","portal item type: "+portalItem.getType());
            }
        });
//        portalItem.loadAsync();
        final ArcGISMapImageLayer mapImageLayer = new ArcGISMapImageLayer("https://gis.fm.umd.edu/arcgis/rest/services/InteriorSpace/GISFloorplansALL/MapServer");
        UserCredential creds2 = new UserCredential("agrawala", "aa1234");
        mapImageLayer.setCredential(creds2);
        mapImageLayer.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Log.v("Map","layer: "+mapImageLayer.getName());

                for(int i=0;i<mapImageLayer.getSublayers().size();i++){
                    Log.v("Map sublayer",mapImageLayer.getSublayers().get(i).getId()+"");
                    for(int j=0;j<mapImageLayer.getSublayers().get(i).getSublayers().size();j++){
                        Log.v("Map sublayer sublayer",mapImageLayer.getSublayers().get(i).getSublayers().get(j).getId()+"");
                    }
                }
                Log.v("Map","layer loaded done");
                Log.v("Map","layer status: "+mapImageLayer.getLoadStatus());
            }
        });
//        mapImageLayer.loadAsync();




        // create a map from a PortalItem
//        while(!portalItem.getLoadStatus().equals("LOADED"));
        Log.v("map","here portal id: "+portalItem.getItemId());
        final ArcGISMap map = new ArcGISMap(portalItem);
        Log.v("Map","map status1: "+map.getLoadStatus());
        //load the map
//        map.loadAsync();
        Log.v("Map","map status2: "+map.getLoadStatus());

        //listen to when it is loaded
        map.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                System.out.println("map loaded done");
                Log.v("Map","map status: "+map.getLoadStatus());
//                map.retryLoadAsync();
            }
        });
        // add map image layer as operational layer
        map.getOperationalLayers().add(mapImageLayer);


        // set the map to be displayed in a MapView
        mMapView.setMap(map);

        Log.v("Map","map status3: "+map.getLoadStatus());

        // Create a LocatorTask using an online locator
        locatorTask = new LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer");
        mGeocodeParameters = new GeocodeParameters();
        mGeocodeParameters.getResultAttributeNames().add("*");
        mGeocodeParameters.setMaxResults(6);

        locatorTask.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if (locatorTask.getLoadStatus() == LoadStatus.LOADED) {
                    // Locator is ready to use
                    Log.v("Map","locatorTask loaded");
//                    // Get LocatorInfo from a loaded LocatorTask
//                    LocatorInfo locatorInfo = locatorTask.getLocatorInfo();
//
//                    // Loop through all the attributes available
//                    for (LocatorAttribute resultAttribute : locatorInfo.getResultAttributes()) {
//                        Log.v("attribute",resultAttribute.getDisplayName());
////            resultAttributeNames.add(resultAttribute.getDisplayName());
//                    }
//
                } else {
                    Log.i("Map", "Trying to reload locator task");
                    locatorTask.retryLoadAsync();
                }
            }
        });

        locatorTask.loadAsync();


//        search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final ListenableFuture<List<GeocodeResult>> geocodeFuture = locatorTask.geocodeAsync(location.getText().toString(),
//                        mGeocodeParameters);
//                geocodeFuture.addDoneListener(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            // Get the results of the async operation
//                            List<GeocodeResult> geocodeResults = geocodeFuture.get();
//
//                            if (geocodeResults.size() > 0) {
//                                // Use the first result - for example
//                                // display on the map
//                                mGeocodedLocation[0] = geocodeResults.get(0);
//                                displaySearchResult(mGeocodedLocation[0].getDisplayLocation(), mGeocodedLocation[0].getLabel());
//
//                                for(int i=0;i<geocodeResults.size();i++){
//                                    Log.v("Map","search: "+geocodeResults.get(i).getDisplayLocation()+" , "+geocodeResults.get(i).getLabel());
//                                }
//
////                                Point point = new Point(mGeocodedLocation[0].getDisplayLocation().getX(),mGeocodedLocation[0].getDisplayLocation().getY(), SpatialReference.create(2229));
////                                Viewpoint viewpoint = new Viewpoint(point,7000);
////                                mMapView.setViewpoint(viewpoint);
//
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Address not found!!", Toast.LENGTH_LONG).show();
//                            }
//
//                        } catch (InterruptedException | ExecutionException e) {
//                            // Deal with exception...
//                            e.printStackTrace();
//                        }
//                        // Done processing and can remove this listener.
//                        geocodeFuture.removeDoneListener(this);
//                    }
//                });


//                Polygon polygon = mMapView.getVisibleArea();
//                Toast.makeText(context,polygon.toJson(),Toast.LENGTH_LONG).show();

//                Geometry gm =(Geometry) Geometry.fromJson("{\n" +
//                        "    \"rings\": [\n" +
//                        "     [\n" +
//                        "      [\n" +
//                        "       -8564514.0826235712,\n" +
//                        "       4720294.659030511\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.6907497682,\n" +
//                        "       4720294.6806521676\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.646406075,\n" +
//                        "       4720287.6727929674\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.9072501417,\n" +
//                        "       4720287.6710404372\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.9014624748,\n" +
//                        "       4720286.7542403964\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.6406185217,\n" +
//                        "       4720286.7558639077\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564510.6238300242,\n" +
//                        "       4720284.1033886811\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564519.6905330699,\n" +
//                        "       4720284.0454856968\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564519.6921502259,\n" +
//                        "       4720284.3073915951\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564520.2792417575,\n" +
//                        "       4720284.303673787\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564520.3642395549,\n" +
//                        "       4720297.7300386578\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564514.1023068782,\n" +
//                        "       4720297.7700379519\n" +
//                        "      ],\n" +
//                        "      [\n" +
//                        "       -8564514.0826235712,\n" +
//                        "       4720294.659030511\n" +
//                        "      ]\n" +
//                        "     ]\n" +
//                        "    ],\n" +
//                        "    \"spatialReference\": {\n" +
//                        "     \"wkid\": 102100,\n" +
//                        "     \"latestWkid\": 3857\n" +
//                        "    }\n" +
//                        "   }");
//                mMapView.setViewpointGeometryAsync(gm);
//            }
//        });

        // get the MapView's LocationDisplay
        final LocationDisplay mLocationDisplay = mMapView.getLocationDisplay();

        //gps button listener
//        gpsbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.RECENTER);
//                if (!mLocationDisplay.isStarted())
//                    mLocationDisplay.startAsync();
//                Log.v("gps",mLocationDisplay.getMapLocation().getX()+","+mLocationDisplay.getMapLocation().getY());
//            }
//        });







        //wifi button listener
        wifibutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create the request queue
                RequestQueue queue = Volley.newRequestQueue(context);

                // Create the request object
                String url = "http://rovermind.cs.umd.edu:8080/LocationServer/FindLocation?type=ap";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                Log.v("Response",response);

                                /*
                                floor   layer no.
                                9       0
                                8       1
                                7       2
                                6       3
                                5       4
                                4       5
                                3       6
                                M2      7
                                2       8
                                M1      9
                                1       10
                                M0      11
                                0       12
                                MB0     13
                                B0      14
                                MSB0    15
                                SB0     16
                                SS0     17

                                 */









                                ///////////// json object(containing lat,long,address string)) --> uncomment below lines for json object format

                             /*   try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    JSONObject lat = jsonResponse.getJSONObject("lat").getDouble();
                                    JSONObject long = jsonResponse.getJSONObject("long").getDouble();
                                    JSONObject address = jsonResponse.getJSONObject("address").getString();
                                    TextView calloutContent = new TextView(getApplicationContext());
                                    calloutContent.setTextColor(Color.BLACK);
                                    calloutContent.setSingleLine();
                                    calloutContent.setText(address);
                                    Viewpoint vp = new Viewpoint(lat,long,700);

                                    mMapView.setViewpointAsync(vp, 1);
                                    Point mapPoint = new Point(long,lat, SpatialReferences.getWgs84());
                                    mCallout = mMapView.getCallout();
                                    mCallout.setLocation(mapPoint);
                                    mCallout.setContent(calloutContent);
                                    mCallout.show();

                                    //deselect all other floor and displaying floor no. 4 only
                                    char firstD = address.charAt(0);
                                    int floor_num = firstD - '0';
                                    int layer_num;
                                    switch (floor_num){
                                        case 0:
                                            layer_num = 12;
                                            break;
                                        case 1:
                                            layer_num = 10;
                                            break;
                                        case 2:
                                            layer_num = 8;
                                            break;
                                        case 3:
                                            layer_num = 6;
                                            break;
                                        case 4:
                                            layer_num = 5;
                                            break;
                                        case 5:
                                            layer_num = 4;
                                            break;
                                        case 6:
                                            layer_num = 3;
                                            break;
                                        case 7:
                                            layer_num = 2;
                                            break;
                                        case 8:
                                            layer_num = 1;
                                            break;
                                        case 9:
                                            layer_num = 0;
                                            break;
                                        case 11: //'B'-'0'=11
                                            layer_num = 14; //basement
                                            break;
                                        default:
                                            layer_num = -1;
                                    }
                                    for(int i=0;i<mapImageLayer.getSublayers().size();i++){
                                        for(int j=0;j<mapImageLayer.getSublayers().get(i).getSublayers().size();j++){
                                            if(j!=layer_num){
                                                mapImageLayer.getSublayers().get(i).getSublayers().get(j).setVisible(false);
                                            }
                                            else{
                                                mapImageLayer.getSublayers().get(i).getSublayers().get(j).setVisible(true);
                                            }
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                */



                             ///////////// comment code lines below if response is in json format

                                // /*
                                TextView calloutContent = new TextView(getApplicationContext());
                                calloutContent.setTextColor(Color.BLACK);
                                calloutContent.setSingleLine();
                                calloutContent.setText(response);
                                Viewpoint vp = new Viewpoint(38.990361,-76.936349,700);

                                // Zoom map to geocode result location
                                mMapView.setViewpointAsync(vp, 1);
                                Point mapPoint = new Point(-76.936349,38.990361, SpatialReferences.getWgs84());
                                mCallout = mMapView.getCallout();
                                mCallout.setLocation(mapPoint);
                                mCallout.setContent(calloutContent);
                                mCallout.show();

                                for(int i=0;i<mapImageLayer.getSublayers().size();i++){
                                    for(int j=0;j<mapImageLayer.getSublayers().get(i).getSublayers().size();j++){
                                        if(j!=5){
                                            mapImageLayer.getSublayers().get(i).getSublayers().get(j).setVisible(false);
                                        }
                                        else{
                                            mapImageLayer.getSublayers().get(i).getSublayers().get(j).setVisible(true);
                                        }
                                    }
                                }
                                // */


                            }
                        },
                        new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO handle the error
                            }

                        }
                ) {

                    @Override
                    public String getBodyContentType() {
                        return "application/xml";
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {

                        String xml = "<?xml version=\"1.0\"?><data>"+"<deviceid>"+LocateMeActivity.deviceID+"</deviceid>";
                        List<ScanResult> results = LocateMeActivity.wifi.getScanResults();
                        Log.v("Number of signals",""+results.size());

                        for (ScanResult result : results) {
                            String mac = result.BSSID;
                            String name = result.SSID;
                            int signalStrength = result.level;
                            int freq = result.frequency;
                            // textStatus.append("\n\n" + result.toString());
                            System.out.println(mac + "," + name+","+signalStrength + "," + freq);
                        }

                        xml = xml + "<accesspoints>";

                        for (ScanResult result : results) {
                            String mac = result.BSSID.trim();
                            String name = result.SSID.trim();
                            int signalStrength = result.level;
                            int freq = result.frequency;
                            xml = xml + "<accesspoint>";
                            xml = xml + "<name>" + name + "</name>";
                            xml = xml + "<mac>" + mac + "</mac>";
                            xml = xml + "<signal>" + signalStrength + "</signal>";
                            xml = xml + "<freq>" + freq + "</freq>";
                            xml = xml + "</accesspoint>";
                        }
                        xml = xml + "</accesspoints></data>";

                        String postData = xml;
                        try {
                            return postData == null ? null :
                                    postData.getBytes(getParamsEncoding());
                        } catch (UnsupportedEncodingException uee) {
                            // TODO consider if some other action should be taken
                            return null;
                        }
                    }
                };

                // Schedule the request on the queue
                queue.add(stringRequest);
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//
//        getMenuInflater().inflate(R.menu.menu_search,menu);
//        MenuItem item = menu.findItem(R.id.menuSearch);
//        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                Log.v("query",query);
//                displaySearchResult(mGeocodedLocation[0].getDisplayLocation(),mGeocodedLocation[0].getLabel());
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(final String newText) {
//                final ListenableFuture<List<GeocodeResult>> geocodeFuture = locatorTask.geocodeAsync(newText,mGeocodeParameters);
//                geocodeFuture.addDoneListener(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            // Get the results of the async operation
//                            List<GeocodeResult> geocodeResults = geocodeFuture.get();
//
//                            if (geocodeResults.size() > 0){
//                                // Use the first result - for example
//                                // display on the map
//                                mGeocodedLocation[0] = geocodeResults.get(0);
//
//                                for(int i=0;i<geocodeResults.size();i++){
//                                    Log.v("Map","search: "+geocodeResults.get(i).getDisplayLocation()+" , "+geocodeResults.get(i).getLabel());
//                                }
//
//                            } else {
//                                if(!newText.isEmpty())
//                                    Toast.makeText(getApplicationContext(), "Address not found!!", Toast.LENGTH_LONG).show();
//                            }
//
//                        } catch (InterruptedException | ExecutionException e) {
//                            // Deal with exception...
//                            e.printStackTrace();
//                        }
//                        // Done processing and can remove this listener.
//                        geocodeFuture.removeDoneListener(this);
//                    }
//                });
//                return false;
//            }
//        });
//
//        return super.onCreateOptionsMenu(menu);
//    }

    void displaySearchResult(Point resultPoint, String address) {

        if (mMapView.getCallout().isShowing()) {
            mMapView.getCallout().dismiss();
        }

        // Zoom map to geocode result location
        mMapView.setViewpointAsync(new Viewpoint(resultPoint, 8000), 2);
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }
}
