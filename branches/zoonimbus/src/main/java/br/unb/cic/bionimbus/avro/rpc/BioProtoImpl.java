package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.*;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.services.storage.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.avro.AvroRemoteException;
import org.apache.zookeeper.KeeperException;

/**
 * Classe de Implementação dos métodos criados na bioproto.avdl, rpc
 * @author zoonimbus
 */
public class BioProtoImpl implements BioProto {

    private final DiscoveryService discoveryService;
    private final StorageService storageService;
    private final SchedService schedService;
    private final ZooKeeperService zkService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedService.class.getSimpleName());

    
    private Map<String, NodeInfo> nodes = new HashMap<String, NodeInfo>();

    @Inject
    public BioProtoImpl(DiscoveryService discoveryService, StorageService storageService, SchedService schedService, ZooKeeperService zkservice) {
        this.discoveryService = discoveryService;
        this.storageService = storageService;
        this.schedService = schedService;
        this.zkService =  zkservice;
    }

    @Override
    public boolean ping() throws AvroRemoteException {
        return true;
    }
    
    /**
     * Retorna o status do job solicitado.
     * @param jobId id do job que deve ser consultado
     * @return string com o status do job
     * @throws AvroRemoteException 
     */
    @Override
    public String statusJob(String jobId) throws AvroRemoteException {
        try {
            if(zkService.getChildren(zkService.getPath().JOBS.getFullPath("", "", ""), null).contains("job_"+jobId)){
                return  "Job "+jobId+" ainda não foi escalonado";
            }else {
                String datas =null;
                ObjectMapper mapper = new ObjectMapper();
                for(PluginInfo plugin : storageService.getPeers().values()){
                    for(String task : zkService.getChildren(zkService.getPath().TASKS.getFullPath(plugin.getId(), "", ""), null)){
                        datas = zkService.getData(zkService.getPath().PREFIX_TASK.getFullPath(plugin.getId(),"",task.substring(5, task.length())),null);
                        if(datas!=null){
                            PluginTask pluginTask = mapper.readValue(datas, PluginTask.class);
                            if(pluginTask.getJobInfo().getId().equals(jobId))
                                return "Job: "+pluginTask.getState().toString();
                        }
                    }
                }
                    
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Job "+jobId+" não encontrado!";
    }

    /**
     * Retorna o status de todos os jobs existentes.
     * @return string com o status de todos os jobs
     * @throws AvroRemoteException 
     */
    @Override
    public String statusAllJob() throws AvroRemoteException {
        StringBuilder allJobs = new StringBuilder();
        int i=1;
        try {
            //verificação dos jobs ainda não escalonados
            List<String> jobs = zkService.getChildren(zkService.getPath().JOBS.getFullPath("", "", ""), null);
            ObjectMapper mapper = new ObjectMapper();
            if(jobs!=null && !jobs.isEmpty()){
                for(String job : jobs){
                        String jobData = zkService.getData(zkService.getPath().PREFIX_JOB.getFullPath("","",job.substring(4,job.length())),null);
                        if(jobData!=null){
                            JobInfo jobInfo = mapper.readValue(jobData, JobInfo.class);
                                allJobs.append(i).append(" - Job ").append(jobInfo.getId()).append(" Ainda não escalonado.\n ");
                        }
                        i++;
                    }
                
            }
            //verificação dos jobs escalonados
            
            String datasTask =null;
            for(PluginInfo plugin : storageService.getPeers().values()){
                for(String task : zkService.getChildren(zkService.getPath().TASKS.getFullPath(plugin.getId(), "", ""), null)){
                    datasTask = zkService.getData(zkService.getPath().PREFIX_TASK.getFullPath(plugin.getId(),"",task.substring(5, task.length())),null);
                    if(datasTask!=null){
                        PluginTask pluginTask = mapper.readValue(datasTask, PluginTask.class);
                        allJobs.append(i).append(" - Job ").append(pluginTask.getJobInfo().getId().toString()).append(" : ").append(pluginTask.getState().toString()).append("\n ");
                    }
                    i++;
                }
            }
                    
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return  allJobs.toString().isEmpty() ? "Não existem jobs." : "Jobs :\n "+allJobs;
    }
    
    
    
    /**
     * Retorna uma lista com o nome dos arquivos pertencentes a toda a federação.
     * @return lista com nome dos arquivos
     * @throws AvroRemoteException 
     */
    @Override
    public List<String> listFilesName() throws AvroRemoteException {
        ArrayList<String> listFile = new ArrayList<String>();
        try {
            for(Collection<String> collection : storageService.getFiles().values()){
                listFile.removeAll(collection);
                listFile.addAll(collection);
            }
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return listFile;
    }
    
    /**
     * 
     * @return
     * @throws AvroRemoteException 
     */
    @Override
    public List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFiles() throws AvroRemoteException {
        
        HashSet<br.unb.cic.bionimbus.avro.gen.PluginFile> listFiles = new HashSet<br.unb.cic.bionimbus.avro.gen.PluginFile>();
        br.unb.cic.bionimbus.avro.gen.PluginFile file=null;
        for(PluginInfo plugin : this.discoveryService.getPeers().values()){
            for(PluginFile fileInfo : storageService.getFilesPeer(plugin.getId())){
                file = new  br.unb.cic.bionimbus.avro.gen.PluginFile();
                file.setId(fileInfo.getId());
                file.setName(fileInfo.getName());
                //retorno com getName  para o path porque avro não reconhece barra(/), adicionar data-folder/
                //ao receber o retono deste método
                file.setPath(fileInfo.getName());
                file.setPluginId(fileInfo.getPluginId());
                file.setSize(fileInfo.getSize());
                listFiles.add(file);
            }    
        }
        List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFile = new ArrayList<br.unb.cic.bionimbus.avro.gen.PluginFile>(listFiles);

        return listFile;
    }
    
    /**
     * Retorna o ip que contém o arquivo informado @param file.
     * Se não encontrar o arquivo retorna null
     * @param file - Nome do arquivo requisitado 
     * @return - Ip de onde o arquivo se encontra, ou caso não encontre retorna null;
     * @throws AvroRemoteException
     */
    @Override
    public String getIpFile(String file){
        
        String destino="";    
        try {
            destino =   storageService.getIpContainsFile(file);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return destino;
    }
    
    @Override
    public List<String> listServices() throws AvroRemoteException {
        Collection<PluginInfo> list = this.discoveryService.getPeers().values();
        List<String> listNameIdService = new ArrayList<String>();

        for(PluginInfo plugin : list){
            for(PluginService pluginService: plugin.getServices()){
                if(!listNameIdService.contains(pluginService.toString()))
                    listNameIdService.add(pluginService.toString());
                
            }
        }
        
        return listNameIdService;
    }
    /**
     * Altera a política de escalonamento para todas os jobs. E retorna a política de escalonamento alterado ou em uso.
     * @param numPolicy, numero da política de escalonamento desejada.   0- AcoSched (Padrão), 1- AHPPolicy, 2- RRPolicy. 
     * Quando valor for -1 deve retornar qual a política de escalonamento está sendo utilizada.
     * @return política de escalonamento
     */
    @Override
    public String schedPolicy(int numPolicy){
        try {
            //verifica se escolher informar a política ou identificar qual é a política
            if(numPolicy==-1){
                numPolicy = new Integer(zkService.getData(zkService.getPath().JOBS.toString(), null));
            }else{
                Integer policy = numPolicy;
                if(zkService.getZNodeExist(zkService.getPath().JOBS.toString(), false)){
                    zkService.setData(zkService.getPath().JOBS.toString(), policy.toString());
                }else{
                    return "\nNão foi possível alterar  política de escalonamento. Tente mais tarde.";
                }
            }
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        StringBuilder politicys = new StringBuilder();
        List<SchedPolicy> listPolicy = SchedPolicy.getInstances();
        for(SchedPolicy policy:listPolicy){
            politicys.append("\n    ").append(policy.getPolicyName());
        }
        
        
        return "\nPolítica Atual: "+listPolicy.get(numPolicy).getPolicyName()+"\n\nPolíticas Disponíveis: "+politicys;
    }
    
    @Override
    public String startJobName(String param, String ip) throws AvroRemoteException {
        final String path = "/jobs/job_";
        JobInfo job = new JobInfo();
        String params[] = param.split(" ");
        String jobId = params[0];
        int i=1;
            
        job.setServiceId(Long.parseLong(jobId));
        job.setLocalId(ip);
        job.setTimestamp(System.currentTimeMillis());
        while (i < params.length) {
            if (i == 1) {
                job.setArgs(params[i]);
                i++;
            } else if (params[i].equals("-i")) {
                i++;
                while (i < params.length && !params[i].equals("-o")) {
                    //verifica a existência dos arquivos de entrada na federação
                    if(!listFilesName().contains(params[i]))
                        return "Job não foi escalonado, arquivo de entrada não existe.";
                    
                    job.addInput(params[i], getPluginFile(params[i]).getSize());
                    i++;
                }
            } else if (params[i].equals("-o")) {
                i++;
                while (i < params.length) {
                    job.addOutput(params[i]);
                    i++;
                }
            }
        }
        LOGGER.info("Tempo de inicio do job -"+ job.getOutputs()+"- MileSegundos: " + job.getTimestamp());
        //inclusão do job para ser escalonado
        zkService.createPersistentZNode(path+job.getId(), job.toString());
        
        return "Job enviado para o escalonamento, Id : "+job.getId()+".\nAguarde...";
    }

    @Override
    public String startJob(List<br.unb.cic.bionimbus.avro.gen.JobInfo> listJob, String ip) throws AvroRemoteException {
        //inclusão do job para ser escalonado
        
        for (br.unb.cic.bionimbus.avro.gen.JobInfo job: listJob){
            job.setTimestamp(System.currentTimeMillis());
            zkService.createPersistentZNode(zkService.getPath().PREFIX_JOB.getFullPath("", "", job.getId()) , job.toString());
            LOGGER.info("Tempo de inicio do job -"+ job.getOutputs()+"- MileSegundos: " + job.getTimestamp());
            //tempo adicionado para latencia poder ser calculada
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        return "Job enviado para o escalonamento. Aguarde...";
    }
    
    private br.unb.cic.bionimbus.avro.gen.PluginFile getPluginFile(String fileName){
        try {
            for(br.unb.cic.bionimbus.avro.gen.PluginFile file : listFiles()){
                if(file.getName().equals(fileName))
                    return file;
            }
        } catch (AvroRemoteException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    
    }
     
    @Override
    public String cancelJob(String jobID) throws AvroRemoteException {
//        schedService.cancelJob(jobID);
        return "Not enabled";
    }
    
    /**
     * Método RPC que pega os peers do zoonimbus e retorna uma lista do tipo NodeInfo
     * @return lista de NodeInfo PeerId, Address, Freesize;
     * @throws AvroRemoteException 
     */
    @Override
    public synchronized List<NodeInfo> getPeersNode() throws AvroRemoteException {

        NodeInfo nodeaux;
        nodes.clear();
        for(PluginInfo info : discoveryService.getPeers().values()){
           nodeaux= new NodeInfo();
           if(info!=null){
                String address = info.getHost().getAddress();
                nodeaux.setAddress(address);

                nodeaux.setPeerId(info.getId());
                nodeaux.setFreesize(info.getFsFreeSize());
                nodes.put(address, nodeaux);
           }       
        }
        
        return new ArrayList<NodeInfo>(nodes.values());
    }

    /**
     * Passa PluginList para StorageService aqui
     * @return bestnodes lista do tipo NodeInfo, retornando os melhores nós da federação
     * @throws AvroRemoteException 
     */
    @Override
    public List<NodeInfo> callStorage(List<NodeInfo> list) throws AvroRemoteException {
        
        List<NodeInfo> bestnodes = storageService.bestNode(list);
        return bestnodes;
    } 
    
    /**
     * Método que cria o znode do arquivo no diretório /pending_save/file_"id_do arquivo" com as informações de arquivos que clientes querem enviar;
     * @param file informações do arquivo:id,nome e tamanho
     * @param kindString Tipo de serviço que está requisitando o arquivo
     * @throws AvroRemoteException 
     */  
    @Override
    public void setFileInfo(FileInfo file, String kindString) {
        PluginFile filePlugin= new PluginFile(file);
        filePlugin.setService(kindString);
        //*Alterar depois caminho para o zookeeperservice
        //verificar se a pasta pending_save existe
        storageService.setPendingFile(filePlugin);
    }
    
    /**
     * Método RPC que retorna o tamanho do arquivo verificado em outro peer;
     * @param file Nome do arquivo
     * @return  size o tamanho do arquivo
     */
    @Override
    public long checkFileSize(String file){
        
       long size = storageService.getFileSize(file);
        
       return size;   
    }
    /**
     * Método avro que chama o método fileuploaded da storage para avisar que o arquivo foi enviado.
     * @param fileSucess informações do arquivo:id,nome e tamanho
     * @param dest lista com os plugins de destino
     */
    @Override
    public synchronized void fileSent(FileInfo fileSucess, List<String> dest){
        PluginFile file = new PluginFile(fileSucess);
        file.setPluginId(dest);
        String pathHome = System.getProperty("user.dir");
        String path =  (pathHome.substring(pathHome.length()).equals("/") ? pathHome+"data-folder/" : pathHome+"/data-folder/");
        file.setPath(path+file.getName());
        try {
            storageService.fileUploaded(file);
        } catch (KeeperException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Método que notifica o peer para fazer a replicação
     * @param filename nome do arquivo a ser replicado
     * @param address  endereço do peer que possui o arquivo
     */
    @Override
    public void notifyReply(String filename, String address) {
        try {
            storageService.replication(filename,address);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSchException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SftpException ex) {
            java.util.logging.Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *Método que verifica se o arquivo existe nó de destino 
     * @param fileSucess
     * @param dest lista de ids 
     * @return 
     */
    @Override
    public boolean verifyFile(FileInfo fileSucess,List dest) {
        PluginFile fileS = new PluginFile(fileSucess);
        fileS.setPluginId(dest);
        return storageService.checkFilePeer(fileS); 
    }


    
    @Override
    public void setWatcher(String idPlugin) {
//        storageService.starWatchers(idPlugin);
    }

 

}
