package br.unb.cic.bionimbus.plugin;

import br.unb.cic.bionimbus.services.ZooKeeperService;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.Callable;

import br.unb.cic.bionimbus.utils.Pair;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginTaskRunner implements Callable<PluginTask> {

    private final AbstractPlugin plugin;
    private final PluginTask task;
    private final PluginService service;
    private final String path;
    private final ZooKeeperService zkService;
    private final String PATHFILES="data-folder/";
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginTaskRunner.class.getSimpleName());


    public PluginTaskRunner(AbstractPlugin plugin, PluginTask task,
                            PluginService service, String path,ZooKeeperService zk) {
        this.plugin = plugin;
        this.service = service;
        this.task = task;
        this.path = path;
        this.zkService = zk;
    }

    
    @Override
    public PluginTask call() throws Exception {

        String args = task.getJobInfo().getArgs();
        List<Pair<String, Long>> inputs = task.getJobInfo().getInputs();
        int i = 1;
        for (Pair<String, Long> pair : inputs) {
            String input = pair.first;
            //linha comentada pois arquivos de entrada não ficam mais no AbstractPlugin
//            args = args.replaceFirst("%I" + i, path + File.pathSeparator + plugin.getInputFiles().get(input).first);
            args = args.replaceFirst("%I" + i, path+PATHFILES + input+" ");
            i++;
        }

        List<String> outputs = task.getJobInfo().getOutputs();
        i = 1;
        for (String output : outputs) {
            args = args.replaceFirst("%O" + i, " "+path+PATHFILES + output);
            i++;
        }
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(service.getPath() + " " + args);
//                        p = Runtime.getRuntime().exec(path+service.getPath().substring(1,service.getPath().length()) + " " + args);

            task.setState(PluginTaskState.RUNNING);
           
            if(zkService!=null)
                zkService.setData(task.getPluginTaskPathZk(), task.toString());
            
            BufferedReader saidaSucesso = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader saidaErro = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;
            int j=0;
            while ((line = saidaSucesso.readLine()) != null && j<6) {
                System.out.println("Job "+task.getJobInfo().getId()+". Saída: "+line);
                j++;
                
            }
            while ((line = saidaErro.readLine()) != null) {
                LOGGER.error("ERRO job ID:" + task.getJobInfo().getId()+"-  Arquivo de saída: "+task.getJobInfo().getOutputs()+", Resposta :"+line);

            }
            
            if(p.waitFor()==0){
                long time =System.currentTimeMillis();
                task.setTimeExec(((float) (time - task.getJobInfo().getTimestamp()) / 1000));
                LOGGER.info("Tempo final do job de saída: "+task.getJobInfo().getOutputs()+" - MileSegundos: " + time);

                task.setState(PluginTaskState.DONE);
            }else {
                task.setTimeExec(((float) (System.currentTimeMillis() - task.getJobInfo().getTimestamp()) / 1000));
                task.setState(PluginTaskState.ERRO);
            }   

            if(zkService!=null)
                zkService.setData(task.getPluginTaskPathZk(), task.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return task;
    }
}
