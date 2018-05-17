package com.dins;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.Properties;

public class SftpClient {


    private static final String REMOTE_IP = "192.168.0.72";
    private static final int REMOTE_PORT = 22;
    private static final String REMOTE_LOGIN = "login";
    private static final String REMOTE_PASSWORD = "password";

    // sec
    private static final int TIME = 60;

    private static final Logger logger = LoggerFactory.getLogger(SftpClient.class);

    private JSch jsch = null;
    private Session session = null;
    private Channel channel = null;
    private ChannelSftp c = null;

    public void connect() throws UnsupportedEncodingException {
        try {
            logger.debug("Initializing jsch");
            jsch = new JSch();
            session = jsch.getSession(REMOTE_LOGIN, REMOTE_IP, REMOTE_PORT);

            session.setPassword(REMOTE_PASSWORD.getBytes("ISO-8859-1"));


            logger.debug("Jsch set to StrictHostKeyChecking=no");
            Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            logger.info("Connecting to " + REMOTE_IP + ":" + REMOTE_PORT);
            session.connect();
            logger.info("Connected !");

            logger.debug("Opening a channel ...");
            channel = session.openChannel("sftp");
            channel.connect();
            c = (ChannelSftp) channel;
            logger.debug("Channel sftp opened");


        } catch (JSchException e) {
            logger.error("", e);
        }
    }

    public void disconnect() {
        if (c != null) {
            logger.debug("Disconnecting sftp channel");
            c.disconnect();
        }
        if (channel != null) {
            logger.debug("Disconnecting channel");
            channel.disconnect();
        }
        if (session != null) {
            logger.debug("Disconnecting session");
            session.disconnect();
        }
    }

    public String runCommand(String command) throws JSchException, IOException, InterruptedException {
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        InputStream in = channelExec.getInputStream();
        channelExec.setCommand(command);
        channelExec.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return reader.readLine();
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, InterruptedException, JSchException {

        DataBase.conn();
        DataBase.createDB();

        double last_cpu = 0;
        double last_ram = 0;
        double last_space = 0;

        int row_cpu = 0;
        int row_ram = 0;
        int row_space = 0;

        SftpClient client = new SftpClient();

        while (true) {
            try {

                client.connect();

                double cpu = Double.parseDouble(client.runCommand("top -bn2 | awk '/Cpu/ {print 100 - $8}' | sed -n 2p"));
                double ram = Double.parseDouble(client.runCommand("free -m | awk '/Mem:/ { printf(\"%3.1f\", $3/$2*100) }'"));
                double space = Double.parseDouble(client.runCommand("df -h / | awk '/\\// {print $(NF-1) - 0}'"));

                DataBase.writeDB(cpu, ram, space);

                row_cpu = last_cpu <= cpu ? row_cpu + 1 : 0;
                last_cpu = cpu;

                row_ram = last_ram <= ram ? row_ram + 1 : 0;
                last_ram = ram;

                row_space = last_space <= space ? row_space + 1 : 0;
                last_space = space;

                if (last_cpu >= 90 || last_ram >= 90 || last_space >= 90) {
                    Email.send("Some of the metrics reached 90%" + "\n" + DataBase.getLastNRows(1));
                } else if ((row_cpu >= 15 && last_cpu > 70)
                        || (row_ram >= 15 && last_ram > 70)
                        || (row_space >= 15 && last_space > 70)) {
                    Email.send("Some of the metrics reached 90%" + "\n" + DataBase.getLastNRows(15));
                }
                logger.debug(DataBase.getLastNRows(1));
            }
            catch (Exception e) {
                logger.error(e.getMessage());
            } finally{
                client.disconnect();
            }
            Thread.sleep(TIME * 1000);
        }
        //com.dins.DataBase.closeDB();
    }
}
