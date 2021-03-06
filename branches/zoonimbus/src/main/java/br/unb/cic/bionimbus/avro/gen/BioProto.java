/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package br.unb.cic.bionimbus.avro.gen;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public interface BioProto {
  public static final org.apache.avro.Protocol PROTOCOL = org.apache.avro.Protocol.parse("{\"protocol\":\"BioProto\",\"namespace\":\"br.unb.cic.bionimbus.avro.gen\",\"types\":[{\"type\":\"enum\",\"name\":\"NodeState\",\"symbols\":[\"STARTING\",\"ACTIVE\",\"CLOSING\",\"IDLE\",\"ERROR\",\"DECOMMISSIONED\"]},{\"type\":\"record\",\"name\":\"JobCancel\",\"fields\":[{\"name\":\"jobID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"files\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}}]},{\"type\":\"record\",\"name\":\"Pair\",\"fields\":[{\"name\":\"first\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"second\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"JobInfo\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"localId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"serviceId\",\"type\":\"long\"},{\"name\":\"args\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"inputs\",\"type\":{\"type\":\"array\",\"items\":\"Pair\"}},{\"name\":\"outputs\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}},{\"name\":\"timestamp\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"PluginFile\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"path\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"name\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"size\",\"type\":\"long\"},{\"name\":\"pluginId\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}}]},{\"type\":\"record\",\"name\":\"NodeInfo\",\"fields\":[{\"name\":\"peerId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"address\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"freesize\",\"type\":\"float\"},{\"name\":\"latency\",\"type\":\"double\"}]},{\"type\":\"record\",\"name\":\"FileInfo\",\"fields\":[{\"name\":\"fileId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"size\",\"type\":\"long\"},{\"name\":\"name\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}],\"messages\":{\"ping\":{\"doc\":\"Pings a node, e.g. to measure latency\",\"request\":[],\"response\":\"boolean\"},\"listFilesName\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}},\"getIpFile\":{\"doc\":\"Procurar em qual Ip está o arquivo file\",\"request\":[{\"name\":\"file\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\"listServices\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}},\"listFiles\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":\"PluginFile\"}},\"startJobName\":{\"request\":[{\"name\":\"jobID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"Ip\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\"startJob\":{\"request\":[{\"name\":\"listJob\",\"type\":{\"type\":\"array\",\"items\":\"JobInfo\"}},{\"name\":\"Ip\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\"schedPolicy\":{\"request\":[{\"name\":\"numPolicy\",\"type\":\"int\"}],\"response\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\"statusJob\":{\"request\":[{\"name\":\"jobId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\"statusAllJob\":{\"request\":[],\"response\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\"cancelJob\":{\"request\":[{\"name\":\"jobID\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},\"setFileInfo\":{\"request\":[{\"name\":\"file\",\"type\":\"FileInfo\"},{\"name\":\"kindService\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":\"null\",\"one-way\":true},\"getPeersNode\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":\"NodeInfo\"}},\"fileSent\":{\"request\":[{\"name\":\"fileSucess\",\"type\":\"FileInfo\"},{\"name\":\"dest\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}}],\"response\":\"null\",\"one-way\":true},\"callStorage\":{\"request\":[{\"name\":\"list\",\"type\":{\"type\":\"array\",\"items\":\"NodeInfo\"}}],\"response\":{\"type\":\"array\",\"items\":\"NodeInfo\"}},\"notifyReply\":{\"request\":[{\"name\":\"filename\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"address\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":\"null\",\"one-way\":true},\"verifyFile\":{\"request\":[{\"name\":\"fileSucess\",\"type\":\"FileInfo\"},{\"name\":\"dest\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}}],\"response\":\"boolean\"},\"setWatcher\":{\"request\":[{\"name\":\"idPlugin\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":\"null\",\"one-way\":true},\"checkFileSize\":{\"request\":[{\"name\":\"file\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}],\"response\":\"long\"}}}");
  /** Pings a node, e.g. to measure latency */
  boolean ping() throws org.apache.avro.AvroRemoteException;
  java.util.List<java.lang.String> listFilesName() throws org.apache.avro.AvroRemoteException;
  /** Procurar em qual Ip está o arquivo file */
  java.lang.String getIpFile(java.lang.String file) throws org.apache.avro.AvroRemoteException;
  java.util.List<java.lang.String> listServices() throws org.apache.avro.AvroRemoteException;
  java.util.List<br.unb.cic.bionimbus.avro.gen.PluginFile> listFiles() throws org.apache.avro.AvroRemoteException;
  java.lang.String startJobName(java.lang.String jobID, java.lang.String Ip) throws org.apache.avro.AvroRemoteException;
  java.lang.String startJob(java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> listJob, java.lang.String Ip) throws org.apache.avro.AvroRemoteException;
  java.lang.String schedPolicy(int numPolicy) throws org.apache.avro.AvroRemoteException;
  java.lang.String statusJob(java.lang.String jobId) throws org.apache.avro.AvroRemoteException;
  java.lang.String statusAllJob() throws org.apache.avro.AvroRemoteException;
  java.lang.String cancelJob(java.lang.String jobID) throws org.apache.avro.AvroRemoteException;
  void setFileInfo(br.unb.cic.bionimbus.avro.gen.FileInfo file, java.lang.String kindService);
  java.util.List<br.unb.cic.bionimbus.avro.gen.NodeInfo> getPeersNode() throws org.apache.avro.AvroRemoteException;
  void fileSent(br.unb.cic.bionimbus.avro.gen.FileInfo fileSucess, java.util.List<java.lang.String> dest);
  java.util.List<br.unb.cic.bionimbus.avro.gen.NodeInfo> callStorage(java.util.List<br.unb.cic.bionimbus.avro.gen.NodeInfo> list) throws org.apache.avro.AvroRemoteException;
  void notifyReply(java.lang.String filename, java.lang.String address);
  boolean verifyFile(br.unb.cic.bionimbus.avro.gen.FileInfo fileSucess, java.util.List<java.lang.String> dest) throws org.apache.avro.AvroRemoteException;
  void setWatcher(java.lang.String idPlugin);
  long checkFileSize(java.lang.String file) throws org.apache.avro.AvroRemoteException;

  @SuppressWarnings("all")
  public interface Callback extends BioProto {
    public static final org.apache.avro.Protocol PROTOCOL = br.unb.cic.bionimbus.avro.gen.BioProto.PROTOCOL;
    /** Pings a node, e.g. to measure latency */
    void ping(org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void listFilesName(org.apache.avro.ipc.Callback<java.util.List<java.lang.String>> callback) throws java.io.IOException;
    /** Procurar em qual Ip está o arquivo file */
    void getIpFile(java.lang.String file, org.apache.avro.ipc.Callback<java.lang.String> callback) throws java.io.IOException;
    void listServices(org.apache.avro.ipc.Callback<java.util.List<java.lang.String>> callback) throws java.io.IOException;
    void listFiles(org.apache.avro.ipc.Callback<java.util.List<br.unb.cic.bionimbus.avro.gen.PluginFile>> callback) throws java.io.IOException;
    void startJobName(java.lang.String jobID, java.lang.String Ip, org.apache.avro.ipc.Callback<java.lang.String> callback) throws java.io.IOException;
    void startJob(java.util.List<br.unb.cic.bionimbus.avro.gen.JobInfo> listJob, java.lang.String Ip, org.apache.avro.ipc.Callback<java.lang.String> callback) throws java.io.IOException;
    void schedPolicy(int numPolicy, org.apache.avro.ipc.Callback<java.lang.String> callback) throws java.io.IOException;
    void statusJob(java.lang.String jobId, org.apache.avro.ipc.Callback<java.lang.String> callback) throws java.io.IOException;
    void statusAllJob(org.apache.avro.ipc.Callback<java.lang.String> callback) throws java.io.IOException;
    void cancelJob(java.lang.String jobID, org.apache.avro.ipc.Callback<java.lang.String> callback) throws java.io.IOException;
    void getPeersNode(org.apache.avro.ipc.Callback<java.util.List<br.unb.cic.bionimbus.avro.gen.NodeInfo>> callback) throws java.io.IOException;
    void callStorage(java.util.List<br.unb.cic.bionimbus.avro.gen.NodeInfo> list, org.apache.avro.ipc.Callback<java.util.List<br.unb.cic.bionimbus.avro.gen.NodeInfo>> callback) throws java.io.IOException;
    void verifyFile(br.unb.cic.bionimbus.avro.gen.FileInfo fileSucess, java.util.List<java.lang.String> dest, org.apache.avro.ipc.Callback<java.lang.Boolean> callback) throws java.io.IOException;
    void checkFileSize(java.lang.String file, org.apache.avro.ipc.Callback<java.lang.Long> callback) throws java.io.IOException;
  }
}