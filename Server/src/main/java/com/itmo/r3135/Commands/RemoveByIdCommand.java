package com.itmo.r3135.Commands;

import com.itmo.r3135.DataManager;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.stream.Collectors;

public class RemoveByIdCommand extends AbstractCommand {

    /**
     * Класс обработки комадны remove_by_id
     */
    public RemoveByIdCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    /**
     * Удаляет элемент по его id.
     */
    @Override
    public ServerMessage activate(Command command) {
        int userId = dataManager.getSqlManager().getUserId(command.getLogin());
        if (userId == -1) return new ServerMessage("Ошибка авторизации!");

        dataManager.getLock().writeLock().lock();
        HashSet<Product> products = dataManager.getProducts();
        int startSize = products.size();
        if (products.size() > 0) {
            int id = command.getIntValue();
            ResultSet resultSet;
            try {
                Statement statement = dataManager.getSqlManager().getConnection().createStatement();
                resultSet = statement.executeQuery(
                        "delete from products where id = " + id + " ,user_id = " + userId + " returgignd id ");

                if (!resultSet.next())
                    products.removeAll((products.parallelStream().filter(product -> product.getId() == id)
                            .collect(Collectors.toCollection(HashSet::new))));
            } catch (SQLException e) {
                return new ServerMessage("Ошибка поиска объектов пользователя в базе.");
            }
            if (startSize == products.size()) {
                dataManager.getLock().writeLock().unlock();
                return new ServerMessage("Элемент с id " + id + " не существует или принадлежит не Вам.");
            }
            dataManager.uptadeDateChange();
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Элемент коллекции успешно удалён.");
        } else {
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Коллекция пуста.");
        }
    }
}
