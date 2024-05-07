#!/usr/bin/python
import pandas as pd

def smallest_key_size(dictionary):
    smallest_key = None
    smallest_size = float('inf')

    for key, value in dictionary.items():
        if isinstance(value, list) and len(value) < smallest_size:
            smallest_key = key
            smallest_size = len(value)
    return smallest_size

def keys_with_size_n(dictionary, size):
    keys = []
    for key, value in dictionary.items():
        if isinstance(value, list) and len(value) > size:
            keys.append(key)
    return keys

def adjusting_dict(dictionary):

    aux_dictionary = dictionary

    len_dic = smallest_key_size(dictionary)
    keys = keys_with_size_n(dictionary, len_dic)

    for key in keys:
        if key in aux_dictionary:

            list_values = aux_dictionary[key]

            # Check if the list has more than len_dic elements
            if len(list_values) > len_dic:

                # Split the list into two parts
                first_list = list_values[::2]
                second_list = list_values[1::2]

                # Create two new keys in the dictionary with the divided parts
                aux_dictionary[key + ' left-right'] = first_list
                aux_dictionary[key + ' right-left'] = second_list

                # Delete the old key
                del aux_dictionary[key]
            else:
                aux_dictionary[key] = list_values

    return aux_dictionary

def normalize_dict(dictionary):
    # Find the maximum size among all the lists
    max_size = max(len(valor) for valor in dictionary.values())

    for key, value in dictionary.items():
        if isinstance(value, list):
            # If the list is smaller than the maximum size, fill it with zeros
            while len(value) < max_size:
                value.append(0)

    return dictionary


# generating sheets with result by id execution
def generating_sheets_result(id_exec, file_path, file_name, file_out, sep):
    df = pd.DataFrame()
    df.to_csv(file_out+'-'+id_exec+'.csv', header=True, sep=';', mode='a', index=False, encoding='utf-8-sig')

    aux_dictionary = {}

    with open(file_path+id_exec+"/"+file_name+".txt") as infile:

        for actual_line in infile:

            parts = actual_line.strip().split(sep)

            if len(parts) == 2:
                column, value = parts[0].strip(), parts[1].strip().replace("s", "")

                if column in aux_dictionary:
                    aux_dictionary[column].append(value)
                else:
                    aux_dictionary[column] = [value]

    final_dic = normalize_dict(adjusting_dict(aux_dictionary))

    #save the last group
    df = pd.DataFrame(final_dic)
    df.to_csv(file_out+'-'+id_exec+'.csv', sep=';', index=False, encoding='utf-8-sig')
