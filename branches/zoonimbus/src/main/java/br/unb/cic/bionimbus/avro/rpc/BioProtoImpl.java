package br.unb.cic.bionimbus.avro.rpc;

import br.unb.cic.bionimbus.avro.gen.BioProto;
import br.unb.cic.bionimbus.avro.gen.FileInfo;
import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.services.sched.SchedService;
import br.unb.cic.bionimbus.services.storage.StorageService;
import br.unb.cic.bionimbus.utils.Put;
import com.google.inject.Inject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.File;
import static java.util.Arrays.asList;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.avro.AvroRemoteException;
import org.apache.zookeeper.KeeperException;

public class BioProtoImpl implements BioProto {

    private final DiscoveryService discoveryService;
    private final StorageService storageService;
    private final SchedService schedService;
    private final ZooKeeperService zkService;
  //  private FileInfo file;
//    private static final Object LOCK = new Object();
    
    private Map<String, NodeInfo> nodes = new HashMap<String, NodeInfo>();

    @Inject
    public BioProtoImpl(DiscoveryService discoveryService, StorageService storageService, SchedService schedService, ZooKeeperService zkservice) {
        this.discoveryService = discoveryService;
        this.storageService = storageService;
        this.schedService = schedService;
        this.zkService =  zkservice;
    }

    public boolean ping() throws AvroRemoteException {
        return true;
    }

    @Override
    public List<String> listFiles() throws AvroRemoteException {
        List<String> listFile = new ArrayList<String>();
        for(Collection<String> collection : storageService.getFiles().values()){
            listFile.addAll(collection);
        }
        
        return listFile;
    }

    @Override
    public List<String> listServices() throws AvroRemoteException {
        Collection<PluginInfo> list = this.discoveryService.getPeers().values();
        List<String> listNameIdService = new ArrayList<String>();

        for(PluginInfo plugin : list){
            for(PluginService pluginService: plugin.getServices()){
                listNameIdService.add(pluginService.toString());
                
            }
        }
        
        return listNameIdService;
    }

    @Override
    public String startJob(String param) throws AvroRemoteException {
        final String path = "/jobs/job_";
        JobInfo job = new JobInfo();
        String params[] = param.split(" ");
        String jobId = params[0];
        int i=1;
            
        job.setServiceId(Long.parseLong(jobId));
        job.setTimestamp(System.currentTimeMillis());
        
        while (i < params.length) {
            if (i == 1) {
                job.setArgs(params[i]);
                i++;
            } else if (params[i].equals("-i")) {
                i++;
                while (i < params.length && !params[i].equals("-o")) {
                    //verifica a existência dos arquivos de entrada na federação
                    if(!listFiles().contains(params[i]))
                        return "Job não foi escalonado, arquivo de entrada não existe.";
                    
                    job.addInput(params[i], Long.valueOf(0));
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
        
        //inclusão do job para ser escalonado
        zkService.createEphemeralZNode(path+job.getId(), job.toString());

        return "Job Escalonado.\n Aguardando execução...";
    }

    @Override
    public String cancelJob(String jobID) throws AvroRemoteException {
        //TODO: call schedService
        return "OK";
    }

    @Override
    public synchronized List<NodeInfo> getPeersNode() throws AvroRemoteException {

        NodeInfo nodeaux;
        //for(PluginInfo info : discoveryService.getPeers())
        nodes.clear();
        for(PluginInfo info : discoveryService.getPeers().values()){
           nodeaux= new NodeInfo();
            //esta setando nulo
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
     *Set the nodes from the clients with latency 
     */
    @Override
    public synchronized Void setNodes(List<NodeInfo> list) throws AvroRemoteException {
           for (NodeInfo node : list) {
              this.nodes.put(node.getAddress(), node);
            }

               for(NodeInfo nodeSeted : this.nodes.values()){
                    //setar o valor na map com o retorno do node
                   PluginInfo plugin = this.discoveryService.getPeers().get(nodeSeted.getPeerId());
                    
                   if(nodeSeted.getLatency()!=null)
                       plugin.setLatency(nodeSeted.getLatency());
               try {
                   zkService.setData(plugin.getPath_zk(), plugin.toString());
               } catch (KeeperException ex) {
                   Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
               } catch (InterruptedException ex) {
                   Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
               }
                        
                    
                }            
        
      return null;
    }

//        public Void sendPlugins(List<NodeInfo> plugins) throws AvroRemoteException {
//        
//        List<? extends Plugin> pluginList = Lists.transform(plugins, new Function<NodeInfo, Plugin>() {
//            
//            private ObjectMapper objectMapper = new ObjectMapper();
//            
//            @Nullable
//            @Override
//            public Plugin apply(@Nullable NodeInfo input) {
//                try {
//                    return objectMapper.readValue(input, Plugin.class);
//                } catch (IOException e) {
//                    return null;
//                }
//
//            }
//        });      
//        
//        return null;  
//   }

    /**
     * Passa PluginList para StorageService aqui
     * @return
     * @throws AvroRemoteException 
     */
    @Override
    public List<NodeInfo> callStorage() throws AvroRemoteException {
        
        List<NodeInfo> bestnodes = storageService.bestNode();
        return bestnodes;
    } 
    
    /**
     * Método que cria o znode do arquivo no diretório /pending_save/file_"id_do arquivo" com as informações de arquivos que clientes querem enviar;
     * @param file
     * @return
     * @throws AvroRemoteException 
     */
    @Override
    public Void setFileInfo(FileInfo file) throws AvroRemoteException {
        PluginFile filePlugin= new PluginFile();
        filePlugin.setId(file.getFileId());
        filePlugin.setName(file.getName());
        //*Alterar depois caminho para o zookeeperservice
        //verificar se a pasta pending_save existe
        filePlugin.setPath("/pending_save/file_"+filePlugin.getId());
        filePlugin.setSize(file.getSize());
        zkService.createPersistentZNode(filePlugin.getPath(), filePlugin.toString());
       return null;
    }
//    public FileInfo getFileInfo(){
//        return this.file;
//    }

    @Override
    public synchronized Void fileSent(FileInfo fileSucess, String dest) throws AvroRemoteException {
        PluginFile file = new PluginFile();
        file.setId(fileSucess.getFileId());
        file.setSize(fileSucess.getSize());
        file.setName(fileSucess.getName());
        file.setPluginId(dest);
        file.setPath("/home/zoonimbus/NetBeansProjects/zoonimbus/data-folder/"+file.getName());
        try {
            storageService.fileUploaded(file);
        } catch (KeeperException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    /**
     *
     * @param pluginList //Lista com os plugins disponiveis para armazenamento
     * @param nodedest // Node de destino onde foi armazenado o primeiro arquivo
     * @param path // Caminho do arquivo
     * @return
     * @throws JSchException
     * @throws SftpException
     */
    
    @Override
    public synchronized Void transferFile(List<NodeInfo> plugins,NodeInfo nodedest,FileInfo file, String path, int copies) throws AvroRemoteException{
        
        storageService.transferFiles(plugins, nodedest, file, path, copies);
        return null;
    }

}
