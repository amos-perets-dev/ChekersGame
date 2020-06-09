package com.example.chekersgamepro.util.network;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class ListenersHolder<Model> {

    private final Set<Model> listeners = new HashSet<>();

    public boolean hasListeners(){
        return !listeners.isEmpty();
    }

    public void clear(){
        listeners.clear();
    }

    public void addListener(Model listener){
        this.listeners.add(listener);
    }

    public void removeListener(Model listener){
        this.listeners.remove(listener);
    }

    public void notifyListeners(ListenersNotifier<Model> listenersNotifier) {
        for(Model listener : new Vector<>(listeners)) {
            try {
                listenersNotifier.notifyListener(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface ListenersNotifier<Model> {
        public void notifyListener(Model listener) throws Exception;
    }
}
