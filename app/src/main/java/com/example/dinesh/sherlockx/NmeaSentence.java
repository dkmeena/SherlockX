package com.example.dinesh.sherlockx;


/**
 * Created by vikrant on 5/9/16.
 */

public class NmeaSentence {
    // National Marine Electronics Association
    private final String GPGGA = "$GPGGA";
    private final String GPGSA = "$GPGSA";
    private final String GPGSV = "$GPGSV";
    public String getGpsMode() {
        String gpsMode = "";
        if (nmeaParts[0].equalsIgnoreCase(GPGSA)) {

            if (nmeaParts.length > 2 && !Strings.isNullOrEmpty(nmeaParts[2])) {
                gpsMode = nmeaParts[2];
            }
        }
        switch (gpsMode) {
            case "1":
                gpsMode = "No Fix Available";
                break;
            case "2":
                gpsMode = "2D";
                break;
            case "3":
                gpsMode = "3D";
                break;
            default:
                gpsMode= "invalide mode = " + gpsMode;
                break;
        }

        return gpsMode;
    }
    /**
     * $GPGSA
     * <p>
     * GPS DOP and active satellites
     * <p>
     * eg1. $GPGSA,A,3,,,,,,16,18,,22,24,,,3.6,2.1,2.2*3C
     * eg2. $GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*35
     * <p>
     * <p>
     * 1    = Mode:
     * M=Manual, forced to operate in 2D or 3D
     * A=Automatic, 3D/2D
     * 2    = Mode:
     * 1=Fix not available
     * 2=2D
     * 3=3D
     * 3-14 = IDs of SVs used in position fix (null for unused fields)
     * 15   = PDOP
     * 16   = HDOP
     * 17   = VDOP
     * <p>
     * $GPGGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx
     * <p>
     * 1, hhmmss.ss = UTC of position
     * llll.ll = latitude of position
     * a = N or S
     * yyyyy.yy = Longitude of position
     * a = E or W
     * x = GPS Quality indicator (0=no fix, 1=GPS fix, 2=Dif. GPS fix)
     * xx = number of satellites in use
     * x.x = horizontal dilution of precision
     * x.x = Antenna altitude above mean-sea-level
     * M = units of antenna altitude, meters
     * x.x = Geoidal separation
     * M = units of geoidal separation, meters
     * x.x = Age of Differential GPS data (seconds)
     * xxxx = Differential reference station ID
     * <p>
     * eg3. $GPGGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh
     * 1    = UTC of Position
     * 2    = Latitude
     * 3    = N or S
     * 4    = Longitude
     * 5    = E or W
     * 6    = GPS quality indicator (0=invalid; 1=GPS fix; 2=Diff. GPS fix)
     * 7    = Number of satellites in use [not those in view]
     * 8    = Horizontal dilution of position
     * 9    = Antenna altitude above/below mean sea level (geoid)
     * 10   = Meters  (Antenna height unit)
     * 11   = Geoidal separation (Diff. between WGS-84 earth ellipsoid and
     * mean sea level.  -=geoid is below WGS-84 ellipsoid)
     * 12   = Meters  (Units of geoidal separation)
     * 13   = Age in seconds since last update from diff. reference station
     * 14   = Diff. reference station ID#
     * 15   = Checksum
     */

    String[] nmeaParts;

    public NmeaSentence(String nmeaSentence) {
        if (Strings.isNullOrEmpty(nmeaSentence)) {
            nmeaParts = new String[]{""};
            return;
        }
        nmeaParts = nmeaSentence.split(",");

    }

    public boolean isLocationSentence() {
        return nmeaParts[0].equalsIgnoreCase(GPGSA) || nmeaParts[0].equalsIgnoreCase(GPGSV) ||
                nmeaParts[0].equalsIgnoreCase(GPGGA);
    }

    public String getLatestPdop() {
        if (nmeaParts[0].equalsIgnoreCase(GPGSA)) {

            if (nmeaParts.length > 15 && !Strings.isNullOrEmpty(nmeaParts[15])) {
                return nmeaParts[15];
            }
        }

        return "";
    }

    public String getLatestVdop() {
        if (nmeaParts[0].equalsIgnoreCase(GPGSA)) {
            if (nmeaParts.length > 17 && !Strings.isNullOrEmpty(nmeaParts[17]) && !nmeaParts[17]
                    .startsWith("*")) {
                return nmeaParts[17].split("\\*")[0];
            }
        }

        return "";
    }


    public String getLatestHdop() {
        if (nmeaParts[0].equalsIgnoreCase(GPGGA)) {
            if (nmeaParts.length > 8 && !Strings.isNullOrEmpty(nmeaParts[8])) {
                return nmeaParts[8];
            }
        } else if (nmeaParts[0].equalsIgnoreCase(GPGSA)) {
            if (nmeaParts.length > 16 && !Strings.isNullOrEmpty(nmeaParts[16])) {
                return nmeaParts[16];
            }
        }

        return "";
    }

    public String getGeoIdHeight() {
        if (nmeaParts[0].equalsIgnoreCase(GPGGA)) {
            if (nmeaParts.length > 11 && !Strings.isNullOrEmpty(nmeaParts[11])) {
                return nmeaParts[11];
            }
        }

        return "";
    }

    public String getAgeOfDgpsData() {
        if (nmeaParts[0].equalsIgnoreCase(GPGGA)) {
            if (nmeaParts.length > 13 && !Strings.isNullOrEmpty(nmeaParts[13])) {
                return nmeaParts[13];
            }
        }

        return "";
    }

    public String getDgpsId() {
        if (nmeaParts[0].equalsIgnoreCase(GPGGA)) {
            if (nmeaParts.length > 14 && !Strings.isNullOrEmpty(nmeaParts[14]) && !nmeaParts[14]
                    .startsWith("*")) {
                return nmeaParts[14].split("\\*")[0];
            }
        }

        return "";
    }

    public String getSatelliteInUse() {

        if (nmeaParts[0].equalsIgnoreCase(GPGGA)) {
            if (nmeaParts.length > 3 && !Strings.isNullOrEmpty(nmeaParts[3])) {
                return nmeaParts[3];
            }
        }
        return "";
    }

    public String getSatelliteInView() {

        if (nmeaParts[0].equalsIgnoreCase(GPGSV)) {
            if (nmeaParts.length > 3 && !Strings.isNullOrEmpty(nmeaParts[3])) {
                return nmeaParts[3];
            }
        }

        return "";
    }

    public void setNmeaSentence(String nmeaSentence) {
        if (Strings.isNullOrEmpty(nmeaSentence)) {
            nmeaParts = new String[]{""};
            return;
        }
        nmeaParts = nmeaSentence.split(",");
    }
}
