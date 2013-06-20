package br.unb.cic.bionimbus.services.storage;

import br.unb.cic.bionimbus.avro.gen.NodeInfo;
import br.unb.cic.bionimbus.avro.rpc.BioProtoImpl;
import br.unb.cic.bionimbus.services.AbstractBioService;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import br.unb.cic.bionimbus.client.FileInfo;
import br.unb.cic.bionimbus.services.messaging.Message;
import br.unb.cic.bionimbus.p2p.P2PEvent;
import br.unb.cic.bionimbus.p2p.P2PEventType;
import br.unb.cic.bionimbus.p2p.P2PMessageEvent;
import br.unb.cic.bionimbus.p2p.P2PMessageType;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.p2p.PeerNode;
import br.unb.cic.bionimbus.p2p.messages.AbstractMessage;
import br.unb.cic.bionimbus.p2p.messages.CloudRespMessage;
import br.unb.cic.bionimbus.p2p.messages.GetReqMessage;
import br.unb.cic.bionimbus.p2p.messages.GetRespMessage;
import br.unb.cic.bionimbus.p2p.messages.ListRespMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreAckMessage;
import br.unb.cic.bionimbus.p2p.messages.StoreReqMessage;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.services.UpdatePeerData;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import br.unb.cic.bionimbus.services.discovery.DiscoveryService;
import br.unb.cic.bionimbus.utils.Put;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.twitter.common.zookeeper.ZooKeeperUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

@Singleton
public class StorageService extends AbstractBioService {

    @Inject
    private MetricRegistry metricRegistry;
    private final ScheduledExecutorService executorService = Executors
            .newScheduledThreadPool(1, new BasicThreadFactory.Builder()
            .namingPattern("StorageService-%d").build());
    private Map<String, PluginInfo> cloudMap = new ConcurrentHashMap<String, PluginInfo>();
    private Map<String, PluginFile> savedFiles = new ConcurrentHashMap<String, PluginFile>();
    private P2PService p2p = null;
    private File dataFolder = new File("data-folder"); //TODO: remover hard-coded e colocar em node.yaml e injetar em StorageService
    private DiscoveryService discoveryService;

    @Inject
    public StorageService(final ZooKeeperService service, MetricRegistry metricRegistry, DiscoveryService disc) {

        Preconditions.checkNotNull(service);
        this.zkService = service;
        this.discoveryService = disc;

        this.metricRegistry = metricRegistry;
        // teste
        Counter c = metricRegistry.counter("teste");
        c.inc();
        
        
        if (!dataFolder.exists()) {
            System.out.println("dataFolder " + dataFolder + " doesn't exists, creating...");
            dataFolder.mkdirs();
            //Recuperar a lista de plugins setados com a latência
           // cloudMap = discoveryService.getPeers();
 //           cloudMap.isEmpty();


        }
    }

    @Override
    public void run() {
        
        System.out.println("Running StorageService...");
       // System.out.println("Cloudmap vazia?:"+cloudMap.values().isEmpty());
        System.out.println("Executando loop.");

        //  System.out.println(" \n Hosts: " + p2p.getConfig().getHost());

//                        Message msg = new CloudReqMessage(p2p.getPeerNode());
//                        p2p.broadcast(msg); // TODO isso e' broadcast?                        

    }

    @Override
    public void start(P2PService p2p) {

        File file = new File("data-folder/persistent-storage.json");
        if (file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, PluginFile> map = mapper.readValue(new File("data-folder/persistent-storage.json"), new TypeReference<Map<String, PluginFile>>() {
                });
                if (filesChanged(map.values())) {
                    map = mapper.readValue(new File("data-folder/persistent-storage.json"), new TypeReference<Map<String, PluginFile>>() {
                    });
                }
                savedFiles = new ConcurrentHashMap<String, PluginFile>(map);
                for(PluginFile filePlugin: savedFiles.values()){
                    zkService.createEphemeralZNode("/peers/peer_"+filePlugin.getPluginId()+"/files/file_"+filePlugin.getId(), filePlugin.toString());  
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.p2p = p2p;
        if (p2p != null) {
            p2p.addListener(this);
        }

        executorService.scheduleAtFixedRate(this, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        p2p.remove(this);
        executorService.shutdownNow();
    }

    @Override
    public void getStatus() {
        // TODO Auto-generated method stub
    }

    @Override
        public void onEvent(P2PEvent event) {
//        
//        
//        if (!event.getType().equals(P2PEventType.MESSAGE)) {
//            return;
//        }
//
//        P2PMessageEvent msgEvent = (P2PMessageEvent) event;
//        Message msg = msgEvent.getMessage();
//        if (msg == null) {
//            return;
//        }
//
//        PeerNode receiver = null;
//        if (msg instanceof AbstractMessage) {
//            receiver = ((AbstractMessage) msg).getPeer();
//        }
//
//        switch (P2PMessageType.of(msg.getType())) {
//            case CLOUDRESP:
//                CloudRespMessage cloudMsg = (CloudRespMessage) msg;
//                /*   DiscoveryService data=new DiscoveryService(zkService);
//                 ConcurrentMap<String,PluginInfo> cloudData= data.getPeers();
//                 for (PluginInfo info : cloudData.values()) {
//                 cloudMap.put(info.getId(), info);
//                 }*/
//                break;
//            case STOREREQ:
//                StoreReqMessage storeMsg = (StoreReqMessage) msg;
//                sendStoreResp(storeMsg.getFileInfo(), storeMsg.getTaskId(), receiver);
//                break;
//            case STOREACK:
//                StoreAckMessage ackMsg = (StoreAckMessage) msg;
//                savedFiles.put(ackMsg.getPluginFile().getId(), ackMsg.getPluginFile());
//                try {
//                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.writeValue(new File("persistent-storage.json"), savedFiles);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//            case LISTREQ:
//                p2p.sendMessage(receiver.getHost(), new ListRespMessage(p2p.getPeerNode(), savedFiles.values()));
//                break;
//            case GETREQ:
//                GetReqMessage getMsg = (GetReqMessage) msg;
//                PluginFile file = savedFiles.get(getMsg.getFileId());
//                // TODO Tem que verificar se o Id do file existe, ou melhor, caso o file seja null deve
//                // exibir uma mensagem de erro avisando que o id do arquivo informado não existe
//                for (PluginInfo plugin : cloudMap.values()) {
//                    if (plugin.getId().equals(file.getPluginId())) {
//                        p2p.sendMessage(receiver.getHost(), new GetRespMessage(p2p.getPeerNode(), plugin, file, getMsg.getTaskId()));
//                        return;
//                    }
//                }
//
//                //TODO mensagem de erro?
//                break;
//        }
    }

    public void sendStoreResp(FileInfo info, String taskId, PeerNode dest) {
        for (PluginInfo plugin : cloudMap.values()) {
            /*if (info.getSize() < plugin.getFsFreeSize()) {
             StoreRespMessage msg = new StoreRespMessage(p2p.getPeerNode(), plugin, info, taskId);
             p2p.sendMessage(dest.getHost(), msg);
             return;
             }*/
        }
    }

    /**  
     * Verifica se os arquivos listados no persistent storage existem, caso não
     * existam é gerado um novo persistent-storage.json
     *
     * @param files
     * @return
     * @throws IOException 
     */
     
    public boolean filesChanged(Collection<PluginFile> files) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Collection<PluginFile> savedFilesOld = files;
        for (PluginFile archive : files) {
            //    System.out.println("nome:" + archive.getPath());
            File arq = new File(archive.getPath());
            if (arq.exists() && checkFiles(archive, savedFilesOld)) {
                savedFiles.put(archive.getId(), archive);
            }
        }
        if (!savedFiles.isEmpty() && !savedFiles.equals(savedFilesOld)) {
            mapper.writeValue(new File("data-folder/persistent-storage.json"), savedFiles);
            return true;
        }
        return false;

    }

    public boolean checkFiles(PluginFile arc, Collection<PluginFile> old) throws IOException {
        for (PluginFile archive_check : old) {
            if (arc.getPath().equalsIgnoreCase(archive_check.getPath())) {
                return true;
            }
        }

        return false;
    }

    public File getDataFolder() {
        return dataFolder;
    }
    

    public void storeFileRec(PluginFile fileC){
        PluginFile file=fileC;
        //file.setPath("data-folder/"+file.getName());
        savedFiles.put(file.getId(),file);
        zkService.createPersistentZNode("/peers/peer_"+file.getPluginId()+"/files/file_"+file.getId(), file.toString());
        try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(new File("data-folder/persistent-storage.json"), savedFiles);
        } catch (Exception e) {
                    e.printStackTrace();
        }
    }
    

    public List<NodeInfo> bestNode(){
        
        List<NodeInfo> plugin;
        cloudMap = discoveryService.getPeers();
        StoragePolicy policy = new StoragePolicy();
        plugin = policy.calcBestCost(zkService,cloudMap.values());

        return plugin;
    }
    
    public void fileUploaded(PluginFile fileuploaded) throws KeeperException, InterruptedException{
        if (zkService.getZNodeExist("/pending_save/file_"+fileuploaded.getId(), false))
        {    
           File fileServer =new File(fileuploaded.getPath());
           if(fileServer.exists()){
               storeFileRec(fileuploaded);
              // zkService.createEphemeralZNode("/peers/peer_"+fileuploaded.getPluginId()+"/files/file_"+fileuploaded.getId(), fileuploaded.toString());            
               zkService.delete("/pending_save/file_"+fileuploaded.getId());
               zkService.getChildren("/pending_save/file_"+fileuploaded.getId(), new UpdatePeerData(zkService,this));
           }else
               System.out.println("Arquivo não encontrado!");
        }
        else
            System.out.println("Arquivo não encontrado nas pendências !");
        
    }
    
    public void transferFiles(List<NodeInfo> plugins,NodeInfo nodedest,br.unb.cic.bionimbus.avro.gen.FileInfo file, String path, int copies){
        
        String dest = null;
        int aux=1;
        
        for (Iterator<NodeInfo> it = plugins.iterator(); it.hasNext();) {
                 NodeInfo node = it.next();
                  
                    Put conexao = new Put(node.getAddress(),path);
                    
                    try {
                        if(conexao.startSession()){
                            dest = node.getPeerId();
                            aux++;
                            if(aux == copies){
                                System.out.println("\n Replication Completed !! No de destino : "+node.getAddress());
                                break;
                            }
                        }
                    } catch (SftpException ex) {
                    Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JSchException ex) {
                         Logger.getLogger(BioProtoImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
        }
        
    }

    @Override
    public void event(WatchedEvent eventType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}