package ktp_lab8;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class CrawlerTask implements Runnable {

    // just alias for depth which is ignored in URLDepthPair compare
    final static int AnyDepth = 0;

    private URLPool m_Pool;
    // there is no '/' at the end of the prefix to also support https
    private String m_Prefix = "http";

    @Override
    public void run() {
        try {
            Scan();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public CrawlerTask(URLPool pool) {
        m_Pool = pool;
    }

    private  void  Scan() throws IOException, InterruptedException {
        while (true) {
            Process(m_Pool.get());

        }
    }

    private void Process(URLDepthPair pair) throws IOException{
        // set up a connection and follow redirect
        URL url = new URL(pair.getURL());
        URLConnection connection = url.openConnection();

        String redirect = connection.getHeaderField("Location");
        if (redirect != null) {
            connection = new URL(redirect).openConnection();
        }

        m_Pool.addProcessed(pair);
        if (pair.getDepth() == 0) return;

        // reading data
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String input;
        while ((input = reader.readLine()) != null) {
            while (input.contains("a href=\"" + m_Prefix)) {
                input = input.substring(input.indexOf("a href=\"" + m_Prefix) + 8);
                String link = input.substring(0, input.indexOf('\"'));
                if(link.contains(" "))
                    link = link.replace(" ", "%20");
                // avoid visiting same link multiple times
                if (m_Pool.getNotProcessed().contains(new URLDepthPair(link, AnyDepth)) ||
                        m_Pool.getProcessed().contains(new URLDepthPair(link, AnyDepth))) continue;
                m_Pool.addNotProcessed(new URLDepthPair(link, pair.getDepth() - 1));
            }
        }
        reader.close();

    }


}

