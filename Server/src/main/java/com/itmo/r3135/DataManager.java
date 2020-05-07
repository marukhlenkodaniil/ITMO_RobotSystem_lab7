package com.itmo.r3135;

import com.itmo.r3135.SQLconnect.MailManager;
import com.itmo.r3135.SQLconnect.SQLManager;
import com.itmo.r3135.World.Product;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataManager {
    private final Date dateInitialization = new Date();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private HashSet<Product> products = new HashSet<>();
    private SQLManager sqlManager;
    private Date dateSave = new Date();
    private Date dateChange = new Date();
    private MailManager mailManager;

    public DataManager() {
    }

    public MailManager getMailManager() {
        return mailManager;
    }

    public void setMailManager(MailManager mailManager) {
        this.mailManager = mailManager;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public void setSqlManager(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public Date getDateChange() {
        return dateChange;
    }

    public HashSet<Product> getProducts() {
        return products;
    }

    public void setProducts(HashSet<Product> products) {
        this.products = products;
    }

    public void uptadeDateChange() {
        this.dateChange = new Date();
    }

    public void updateDateSave() {
        this.dateSave = new Date();
    }

    @Override
    public String toString() {
        return "------------------------" +
                "\nИнформация о коллекции:" +
                "\n------------------------" +
                "\n Количество элементов коллекции: " + products.size() +
                "\n Дата инициализации: " + dateInitialization +
                "\n Дата последнего сохранения: " + dateSave +
                "\n Дата последнего изменения: " + dateChange;
    }

    public void updateDateChange() {
        this.dateSave = new Date();
    }
}
