cntFriendOfFriends = {}
cntTotFriends = {}


def my_compare_other(x, y):
    if cntTotFriends[x] > cntTotFriends[y]:
        return -1
    elif cntTotFriends[x] < cntTotFriends[y]:
        return 1
    else:
        if x < y:
            return -1
        else:
            return 1


def my_compare(x, y):
    x_pres = x in cntFriendOfFriends
    y_pres = y in cntFriendOfFriends
    if x_pres and y_pres:
        if cntFriendOfFriends[x] > cntFriendOfFriends[y]:
            return -1
        elif cntFriendOfFriends[x] < cntFriendOfFriends[y]:
            return 1
        else:
            return my_compare_other(x, y)
    elif (not x_pres) and y_pres:
        return 1
    elif x_pres and (not y_pres):
        return -1
    else:
        return my_compare_other(x, y)


def my_suggestions(id_of_person, adj_list):
    friends = {}
    not_friends = []
    suggestions_order_2d = []
    users_friends = []
    cntFriendOfFriends.clear()
    cntTotFriends.clear()

    for i in range(len(adj_list)):
        if adj_list[i][0] != id_of_person:
            continue
        else:
            for j in range(1, len(adj_list[i])):
                friends[adj_list[i][j]] = 1
                users_friends.append(adj_list[i][j])
    for i in range(len(adj_list)):
        if adj_list[i][0] != id_of_person:
            if not adj_list[i][0] in friends:
                not_friends.append(adj_list[i][0])
                cntTotFriends[adj_list[i][0]] = len(adj_list[i]) - 1
    for i in range(len(adj_list)):
        if adj_list[i][0] != id_of_person:
            if adj_list[i][0] in friends:
                for j in range(1, len(adj_list[i])):
                    if (not adj_list[i][j] in friends) and (adj_list[i][j] != id_of_person):
                        if adj_list[i][j] in cntFriendOfFriends:
                            cntFriendOfFriends[adj_list[i][j]] += 1
                        else:
                            cntFriendOfFriends[adj_list[i][j]] = 1
    # set limit to required number of suggestions
    limit = float('inf')
    """ For python 2.7 and newer versions """
    suggestions_order = sorted(not_friends, my_compare)
    suggestions_order_2d.append(users_friends)
    suggestions_order_2d.append(suggestions_order[:min(limit, len(suggestions_order))])
    return suggestions_order_2d


def give_suggestions(id_of_person, users):
    adj_list = []
    for user in users:
        temp = [user.pk]
        for user_friend in user.friends.all():
            temp.append(user_friend.pk)  # friend pk
        adj_list.append(temp)
    suggestions_as_pk = my_suggestions(id_of_person, adj_list)
    sorted_list = []
    sorted_list_of_friends = []
    sorted_list_of_others = []
    for i in range(len(suggestions_as_pk[0])):
        for user in users:
            if user.pk == suggestions_as_pk[0][i]:
                sorted_list_of_friends.append(user)
                break
    sorted_list.append(sorted_list_of_friends)
    for i in range(len(suggestions_as_pk[1])):
        for user in users:
            if user.pk == suggestions_as_pk[1][i]:
                sorted_list_of_others.append(user)
                break
    sorted_list.append(sorted_list_of_others)
    return sorted_list
