package br.unb.cic.bionimbus.client;

import java.util.UUID;

/**
 * Classe que gera as informações do arquivo na parte cliente;
 * @author breno-linux
 */
public class FileInfo {
    private String id= UUID.randomUUID().toString();
    private String name;
    private long size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
}
