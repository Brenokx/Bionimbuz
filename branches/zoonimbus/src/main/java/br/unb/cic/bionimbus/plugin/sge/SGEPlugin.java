package br.unb.cic.bionimbus.plugin.sge;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import br.unb.cic.bionimbus.p2p.Host;
import br.unb.cic.bionimbus.p2p.P2PService;
import br.unb.cic.bionimbus.plugin.AbstractPlugin;
import br.unb.cic.bionimbus.plugin.PluginFile;
import br.unb.cic.bionimbus.plugin.PluginGetFile;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginService;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.plugin.PluginTaskRunner;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetFile;
import br.unb.cic.bionimbus.plugin.linux.LinuxGetInfo;
import br.unb.cic.bionimbus.plugin.linux.LinuxSaveFile;
import br.unb.cic.bionimbus.services.ZooKeeperService;
import java.io.IOException;

public class SGEPlugin extends AbstractPlugin {

    private final ExecutorService executorService = Executors
            .newCachedThreadPool(new BasicThreadFactory.Builder()
                    .namingPattern("SGEPlugin-workers-%d").build());

    public SGEPlugin(P2PService p2p) throws IOException {
        super(p2p);
    }

    @Override
    protected Future<PluginInfo> startGetInfo() {
        return executorService.submit(new LinuxGetInfo());
    }

    @Override
    protected Future<PluginFile> saveFile(String filename) {
        return executorService.submit(new LinuxSaveFile(filename));
    }

    @Override
    protected Future<PluginGetFile> getFile(Host origin, PluginFile file, String taskId, String savePath) {
        return executorService.submit(new LinuxGetFile(file, taskId, origin, savePath));
    }

    @Override
    public Future<PluginTask> startTask(PluginTask task, ZooKeeperService zk) {
        PluginService service = getMyInfo().getService(task.getJobInfo().getServiceId());
        if (service == null)
            return null;

        return executorService.submit(new PluginTaskRunner(this, task, service, getP2P().getConfig().getServerPath(),zk));
    }

    @Override
    public void start() {
        System.out.println("starting SGE plugin...");
    }

}
