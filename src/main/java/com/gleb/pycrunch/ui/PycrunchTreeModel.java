package com.gleb.pycrunch.ui;



import com.gleb.pycrunch.PycrunchTestMetadata;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class PycrunchTreeModel  implements TreeModel {
    // Список потомков корневой записи
    private ArrayList<String> rootList = new ArrayList<String>();
    // Дочерние узлы первого уровня
    private ArrayList<String>[] tnodes;
    String     root  = "Root (invisible)";
//    String[]  nodes = new String[]  {"Напитки", "Сладости"};
//    String[][] leafs = new String[][]{{"Чай", "Кофе", "Пиво", "Минералка"},
//            {"Пирожное", "Мороженое", "Зефир", "Халва"}};

    public PycrunchTreeModel(ArrayList<PycrunchTestMetadata> tests)
    {
        HashMap<String, ArrayList<PycrunchTestMetadata>> modules = new HashMap<>();
        for (PycrunchTestMetadata t : tests) {
            ArrayList<PycrunchTestMetadata> my;
            if (!modules.containsKey(t.module)) {
                modules.put(t.module, new ArrayList<>());
            }
            my = modules.get(t.module);
            my.add(t);
        }

        // Заполнение списков данными
        int size = modules.keySet().size();
        Object[] module_names = modules.keySet().toArray();
        tnodes = (ArrayList<String>[]) new ArrayList<?>[size];
        for (int i = 0; i < size; i++) {
            String current_module = (String) module_names[i];
            rootList.add(current_module);
            tnodes[i] = new ArrayList<String>();
            Object[] pycrunchTestMetadata = modules.get(current_module).toArray();
            for (int j = 0; j < pycrunchTestMetadata.length; j++) {
                tnodes[i].add(((PycrunchTestMetadata)pycrunchTestMetadata[j]).name);
            }
        }
    }
    // Функция получения корневого узла дерева
    @Override
    public Object getRoot() {
        return root;
    }
    // Функция получения потомка корневого узла
    private final int getRootChild(Object node)
    {
        int idx = -1;
        for (int i = 0; i < rootList.size(); i++){
            if (rootList.get(i) == node) {
                idx = i;
                break;
            }
        }
        return idx;
    }
    // Функция получения количество потомков узла
    @Override
    public int getChildCount(Object node)
    {
        int idx = getRootChild(node);
        if ( node == root )
            return rootList.size();
        else if ( node == rootList.get(idx))
            return tnodes[idx].size();
        return 0;
    }
    // Функция получения потомка узла по порядковому номеру
    @Override
    public Object getChild(Object node, int index)
    {
        int idx = getRootChild(node);
        if ( node == root )
            return rootList.get(index);
        else if ( node == rootList.get(idx))
            return tnodes[idx].get(index);
        return null;
    }
    // Функция получения порядкового номера потомка
    @Override
    public int getIndexOfChild(Object node, Object child)
    {
        int idx = getRootChild(node);
        if ( node == root )
            return rootList.indexOf(child);
        else if ( node == rootList.get(idx))
            return tnodes[idx].indexOf(child);
        return 0;
    }
    // Функция определения, является ли узел листом
    @Override
    public boolean isLeaf(Object node)
    {
        int idx = getRootChild(node);
        if ((idx >= 0) && tnodes[idx].contains(node))
            return true;
        else
            return false;
    }
    // Функция вызывается при изменении значения некоторого узла
    public void valueForPathChanged(TreePath path, Object value) {}
    // Метод присоединения слушателя
    @Override
    public void addTreeModelListener(TreeModelListener tml) {}
    // Методы удаления слушателя
    @Override
    public void removeTreeModelListener(TreeModelListener tml) {}
}

