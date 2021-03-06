package br.unb.cic.bionimbus.client.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.rpc.RpcClient;
import br.unb.cic.bionimbus.client.shell.commands.*;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A simple shell to interface with BioNimbus. Lacking features: No up arrow
 * down arrow (circular buffer) No pipe | or redirection > No autocomplete
 */
public final class SimpleShell {

    private static final String GREETINGS = "Welcome to BioNimbus shell\nversion 0.0.2";
    private static final String PROMPT = "[@bionimbus]$ ";
    private static final Map<String, Command> commandMap = new HashMap<String, Command>();
    public static History history = new History(10);
    private RpcClient rpcClient;

    static {
        commandMap.put(DateTime.NAME, new DateTime());
        commandMap.put(Help.NAME, new Help(commandMap));
        commandMap.put(History.NAME, history);
        commandMap.put(Echo.NAME, new Echo());
    }
    private boolean connected = false;
    private BioProto proxy;
    private String ip;

    public SimpleShell() {
        //whereami - prints the host it's connected to
        // jobs - print the list of executing jobs
        // file <aa> - print details about a given file
        // disconnect
        // nodes - print the ids and addresses of nodes (grab from zk)

//        commandMap.put(PingCommand.NAME, new PingCommand(this));
        commandMap.put(ListCommands.NAME, new ListCommands(this));
        commandMap.put(Connect.NAME, new Connect(this));
        commandMap.put(ListFiles.NAME, new ListFiles(this));
        commandMap.put(ListServices.NAME, new ListServices(this));
        commandMap.put(ListPeersCommand.NAME, new ListServices(this));
        commandMap.put(Quit.NAME, new Quit(this));

        commandMap.put(SchedulingPolicy.NAME, new SchedulingPolicy(this));
        commandMap.put(JobStart.NAME, new JobStart(this));
        commandMap.put(JobStatus.NAME, new JobStatus(this));

        commandMap.put(JobCancel.NAME, new JobCancel(this));
        //PingCommand
        //GetFile
        commandMap.put("script", new ScriptRunner(this));
        commandMap.put(Upload.NAME, new Upload(this));
        commandMap.put(Download.NAME, new Download(this));
    }

    public void registerCommand(Command command) {
        commandMap.put(command.getName(), command);
    }

    public void setRpcClient(RpcClient client) {
        this.rpcClient = client;
    }

    public RpcClient getRpcClient() {
        return rpcClient;
    }

    public static void main(String[] args) throws IOException {
        new SimpleShell().readEvalPrintLoop();
    }

    public void print(String message) {
        System.out.println('\n' + message);
        System.out.print(PROMPT);
    }

    private void readEvalPrintLoop() throws IOException {

        System.out.println(GREETINGS);
        while (true) {
            // readco
            System.out.print(PROMPT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line = reader.readLine().trim();

            if (line.startsWith("!")) {
                Long number = Long.parseLong(line.substring(1));
                line = history.get(number);
            }

            executeCommand(line, true);
        }
    }

    public void executeCommand(String line, boolean logAtHistory) {

        if (logAtHistory) {
            history.add(line.trim());
        }

        Pair<String, String[]> command = parseLine(line);

        if (!commandMap.containsKey(command.first)) {
            System.out.println(String.format("%s: command not found", command.first));
        } else {
            try {

                commandMap.get(command.first).setOriginalParamLine(line); // para o caso de precisar

                // eval
                String result = commandMap.get(command.first).execute(command.second);

                // print
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getIp() {
        return this.ip;
    }
    
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    private static Pair<String, String[]> parseLine(String line) {

        LineParser parser = new LineParser();
        List<String> tokens = parser.parse(line);

        final String command = tokens.get(0);
        final List<String> params = new ArrayList<String>();

        if (tokens.size() > 1) {
            params.addAll(tokens.subList(1, tokens.size()));
        }

        return Pair.of(command, params.toArray(new String[0]));
    }

    public Collection<Command> getCommands() {
        return Collections.unmodifiableCollection(commandMap.values());
    }

    public BioProto getProxy() throws IOException {
        return rpcClient.getProxy();
    }
    public void close() throws IOException {
        try {
            rpcClient.close();
        } catch (Exception ex) {
            Logger.getLogger(SimpleShell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
