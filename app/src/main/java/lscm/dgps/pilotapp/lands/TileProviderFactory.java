package lscm.dgps.pilotapp.lands;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

public class TileProviderFactory {

	public static GeoServerTileProvider getGeoServerTileProvider() {

        //String baseURL = "http://192.168.7.179:8443/geoserver/wms?";
		
		String baseURL = "http://202.189.117.179:8443/geoserver/wms?";
        String version = "1.1.1";
        String request = "GetMap";
        String format = "image/png";
        String srs = "EPSG:900913";
        String service = "WMS";
        String width = "256";
        String height = "256";
        String styles = "";
        String layers = "mapHK:Sea,mapHK:Land,mapHK:Plac_text1,mapHK:Plac_text2,mapHK:Plac_text3,mapHK:Park,mapHK:park_text2,mapHK:Rail,mapHK:Rail1_text,mapHK:Rail2_text,mapHK:Road1,mapHK:road1_text,mapHK:Road2,mapHK:road2_text,mapHK:Road3,mapHK:Road4,mapHK:road4_text,mapHK:BLDG,mapHK:BLDG_text";

        final String URL_STRING = baseURL + 
                "&layers=" + layers +
                "&version=" + version + 
                "&service=" + service + 
                "&request=" + request + 
                "&styles=" + styles + 
                "&format=" + format + 
                "&srs=" + srs + 
                "&bbox=%f,%f,%f,%f" + 
                "&width=" + width + 
                "&height=" + height +
                "&transparent=true";


        GeoServerTileProvider tileProvider = 
            new GeoServerTileProvider(256,256) {

            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                try {       

                    double[] bbox = getBoundingBox(x, y, zoom);

                    String s = String.format(Locale.US, URL_STRING,
                    		bbox[MINX], 
                            bbox[MINY], 
                            bbox[MAXX], 
                            bbox[MAXY]);

                    Log.d("GeoServerTileURL", s);

                    URL url = null;

                    try {
                        url = new URL(s);

                        Log.d("return url", 
                                "protocol = " + url.getProtocol() +
                                "Host = " + url.getHost() +
                                "prot = " + String.valueOf(url.getPort()) +
                                "Path = " + url.getPath() +
                                "Query = " + url.getQuery());
                    } 
                    catch (MalformedURLException e) {
                    	Log.d("Change to URL exception", e.getMessage());                    	
                        throw new AssertionError(e);
                    }

                    return url;
                }
                catch (RuntimeException e) {
                    Log.d("GeoServerTileException", 
                        "getTile x=" + x + ", y=" + y + 
                        ", zoomLevel=" + zoom + 
                        " raised an exception", e);
                    throw e;
                }

            }
        };
        return tileProvider;
    }
	
	public static GeoServerTileProvider getGeoServerTileProvider(String ipaddress,long gc_str_landMapServerPort) {

        //String baseURL = "http://192.168.7.179:8443/geoserver/wms?";
		
		//String baseURL = "http://202.189.117.179:8443/geoserver/wms?";
        String version = "1.1.1";
        String request = "GetMap";
        String format = "image/png";
        String srs = "EPSG:900913";
        String service = "WMS";
        String width = "256";
        String height = "256";
        String styles = "";
        String layers = "mapHK:Sea,mapHK:Land,mapHK:Plac_text1,mapHK:Plac_text2,mapHK:Plac_text3,mapHK:Park,mapHK:park_text2,mapHK:Rail,mapHK:Rail1_text,mapHK:Rail2_text,mapHK:Road1,mapHK:road1_text,mapHK:Road2,mapHK:road2_text,mapHK:Road3,mapHK:Road4,mapHK:road4_text,mapHK:BLDG,mapHK:BLDG_text";

        final String URL_STRING = "http://" + ipaddress + ":" + gc_str_landMapServerPort + "/geoserver/wms?" +
                "&layers=" + layers +
                "&version=" + version + 
                "&service=" + service + 
                "&request=" + request + 
                "&styles=" + styles + 
                "&format=" + format + 
                "&srs=" + srs + 
                "&bbox=%f,%f,%f,%f" + 
                "&width=" + width + 
                "&height=" + height +
                "&transparent=true";


        GeoServerTileProvider tileProvider = 
            new GeoServerTileProvider(256,256) {

            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                try {       

                    double[] bbox = getBoundingBox(x, y, zoom);

                    String s = String.format(Locale.US, URL_STRING,
                    		bbox[MINX], 
                            bbox[MINY], 
                            bbox[MAXX], 
                            bbox[MAXY]);

                    Log.d("GeoServerTileURL", s);

                    URL url = null;

                    try {
                        url = new URL(s);

                        Log.d("return url", 
                                "protocol = " + url.getProtocol() +
                                "Host = " + url.getHost() +
                                "prot = " + String.valueOf(url.getPort()) +
                                "Path = " + url.getPath() +
                                "Query = " + url.getQuery());
                    } 
                    catch (MalformedURLException e) {
                    	Log.d("Change to URL exception", e.getMessage());                    	
                        throw new AssertionError(e);
                    }

                    return url;
                }
                catch (RuntimeException e) {
                    Log.d("GeoServerTileException", 
                        "getTile x=" + x + ", y=" + y + 
                        ", zoomLevel=" + zoom + 
                        " raised an exception", e);
                    throw e;
                }

            }
        };
        return tileProvider;
    }
}
