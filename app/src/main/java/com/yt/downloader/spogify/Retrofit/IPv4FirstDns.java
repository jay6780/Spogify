package com.yt.downloader.spogify.Retrofit;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

public class IPv4FirstDns implements Dns {
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        try {
            InetAddress[] inetAddresses = InetAddress.getAllByName(hostname);
            List<InetAddress> reorderedList = new ArrayList<>();

            for (InetAddress address : inetAddresses) {
                if (address instanceof Inet4Address) {
                    reorderedList.add(address);
                }
            }
            for (InetAddress address : inetAddresses) {
                if (!(address instanceof Inet4Address)) {
                    reorderedList.add(address);
                }
            }
            
            return reorderedList;
        } catch (NullPointerException e) {
            UnknownHostException unknownHostException = 
                new UnknownHostException("Broken system behaviour for dns lookup of " + hostname);
            unknownHostException.initCause(e);
            throw unknownHostException;
        }
    }
}