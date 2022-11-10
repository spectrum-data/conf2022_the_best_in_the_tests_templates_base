##### Owner`s login:andreygubkin
##### All basic tests were passed

##### Your own tests: 29/29
##### So, 29 test(s) can get you points

##### Competitors:
###### Shultzenfegel: you passed 11/39
###### battle-toads: you passed 14/68
###### comdiv: you passed 26/30

##### FULL_INFO
|author|input|expected|result|
|-----|-----|-----|-----|
|andreygubkin|6678098178|~?INN_UL+:6678098178|true|
|andreygubkin|0023456784|~?INN_UL-:0023456784|true|
|andreygubkin|0123456789|~?INN_UL-:0123456789|true|
|andreygubkin|610108091503|~?INN_FL+:610108091503|true|
|andreygubkin|000108091500|~?INN_FL-:000108091500|true|
|andreygubkin|610108091502|~?INN_FL-:610108091502|true|
|andreygubkin|предъявите документы!|==NOT_FOUND|true|
|andreygubkin|65 04 346372|~?PASSPORT_RF+:6504346372|true|
|andreygubkin|да это точно мой паспорт: 6504346123|==NOT_FOUND|true|
|andreygubkin|6504346372|~?PASSPORT_RF+:6504346372|true|
|andreygubkin|5102998293|~?DRIVER_LICENSE+:5102998293|true|
|andreygubkin|51 02 998293|~?DRIVER_LICENSE+:5102998293|true|
|andreygubkin|В 123 ХХ 123|==GRZ+:В123ХХ123|true|
|andreygubkin|В123ХХ123|==GRZ+:В123ХХ123|true|
|andreygubkin|В 123 ХХ 20|==GRZ+:В123ХХ20|true|
|andreygubkin|В123ХХ20|==GRZ+:В123ХХ20|true|
|andreygubkin|В000ХХ20|==GRZ-:В000ХХ20|true|
|andreygubkin|В123ХХ00|==GRZ-:В123ХХ00|true|
|andreygubkin|0123456789ABCDEFG|~?VIN+:0123456789ABCDEFG|true|
|andreygubkin|0123456789ABCDEFI|~?VIN-:0123456789ABCDEFI|true|
|andreygubkin|0123456789ABCDEFO|~?VIN-:0123456789ABCDEFO|true|
|andreygubkin|0123456789ABCDEFQ|~?VIN-:0123456789ABCDEFQ|true|
|andreygubkin|1186658094094|~?OGRN+:1186658094094|true|
|andreygubkin|321665800075935|~?OGRNIP+:321665800075935|true|
|andreygubkin|1186658094093|~?OGRN-:1186658094093|true|
|andreygubkin|321665800075931|~?OGRNIP-:321665800075931|true|
|andreygubkin|04480560348|~?SNILS+:044-805-603-48|true|
|andreygubkin|044-805-603-48|~?SNILS+:044-805-603-48|true|
|andreygubkin|04480560347|~?SNILS-:044-805-603-47|true|
|Shultzenfegel|1234 567890|~?PASSPORT_RF:1234567890,DRIVER_LICENSE:1234567890|true|
|Shultzenfegel|паспорт или ВУ: 5050 909 012|~?PASSPORT_RF:5050909012,DRIVER_LICENSE:5050909012|false|
|Shultzenfegel|5401 425555|~?PASSPORT_RF+|true|
|Shultzenfegel|5411 425555|~?PASSPORT_RF+|true|
|Shultzenfegel|5421 425555|~?PASSPORT_RF+|true|
|Shultzenfegel|5441 425555|~?PASSPORT_RF-|false|
|Shultzenfegel|5451 425555|~?PASSPORT_RF-|false|
|Shultzenfegel|5461 425555|~?PASSPORT_RF-|false|
|Shultzenfegel|5471 425555|~?PASSPORT_RF-|false|
|Shultzenfegel|5481 425555|~?PASSPORT_RF-|false|
|Shultzenfegel|5491 425555|~?PASSPORT_RF-|false|
|Shultzenfegel|0011 256587|~?PASSPORT_RF-:0011256587|false|
|Shultzenfegel|0011 256587|~?DRIVER_LICENSE-:0011256587|false|
|Shultzenfegel|9210 123321|~?PASSPORT_RF+,DRIVER_LICENSE+|true|
|Shultzenfegel|9510 123321|~?PASSPORT_RF+,DRIVER_LICENSE+|true|
|Shultzenfegel|9010 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|9110 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|9310 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|9410 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|9610 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|9710 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|9810 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|9910 123321|~?PASSPORT_RF-,DRIVER_LICENSE-|false|
|Shultzenfegel|серия 5010 номер 531432|~?PASSPORT_RF:5010531432,DRIVER_LICENSE:5010531432|false|
|Shultzenfegel|Номер а123вс96|==GRZ+:А123ВС96|false|
|Shultzenfegel|ГРЗ К456УН177|==GRZ+:К456УН177|false|
|Shultzenfegel|Рег. номер m123pe40|==GRZ+:М123РЕ40|false|
|Shultzenfegel|Номер ТС П789ЫЦ66|==GRZ-|false|
|Shultzenfegel|Номер регистрации Р987ТС 166 какие-то слова|==GRZ+:Р987ТС166|false|
|Shultzenfegel|Номер регистрации Р+987-ТС_166 какие-то слова|==GRZ+:Р987ТС166|false|
|Shultzenfegel|1137746185818|~?OGRN+:1137746185818|true|
|Shultzenfegel|1-13-77-4618581-9|~?OGRN-:1137746185819|false|
|Shultzenfegel|304500116000157|~?OGRNIP+:304500116000157|true|
|Shultzenfegel|304500116000158|~?OGRNIP-:304500116000158|true|
|Shultzenfegel|660688767174|~?INN_FL+:660688767174|true|
|Shultzenfegel|660788767174|~?INN_FL+:660788767174|false|
|Shultzenfegel|0719560114|~?INN_UL+:0719560114|true|
|Shultzenfegel|0729560114|~?INN_UL+:0729560114|false|
|Shultzenfegel|Гос. номер C789TX66|==GRZ+:С789ТХ66|false|
|battle-toads|VIN X1W932700V0000657|==VIN+:X1W932700V0000657|false|
|battle-toads|VIN 11111111111111111|==VIN-:11111111111111111|false|
|battle-toads|ХW8ZZZ5NZMG023644 Lada Priora|==VIN+:XW8ZZZ5NZMG023644|false|
|battle-toads|75 12 211133|~?PASSPORT_RF+:7512211133|true|
|battle-toads|31 12 288813|~?PASSPORT_RF-:3112288813|false|
|battle-toads|47 66 032123|~?PASSPORT_RF-:4766032123|false|
|battle-toads|02 09 132123|~?PASSPORT_RF-:0209132123|false|
|battle-toads|06 09 132123|~?PASSPORT_RF-:0609132123|false|
|battle-toads|09 09 132123|~?PASSPORT_RF-:0909132123|false|
|battle-toads|13 09 132123|~?PASSPORT_RF-:1309132123|false|
|battle-toads|16 09 132123|~?PASSPORT_RF-:1609132123|false|
|battle-toads|21 09 132123|~?PASSPORT_RF-:2109132123|false|
|battle-toads|23 09 132123|~?PASSPORT_RF-:2309132123|false|
|battle-toads|31 09 132123|~?PASSPORT_RF-:3109132123|false|
|battle-toads|39 09 132123|~?PASSPORT_RF-:3909132123|false|
|battle-toads|43 09 132123|~?PASSPORT_RF-:4309132123|false|
|battle-toads|51 12 122123|~?PASSPORT_RF-:5112122123|false|
|battle-toads|55 12 122123|~?PASSPORT_RF-:5512122123|false|
|battle-toads|59 12 122123|~?PASSPORT_RF-:5912122123|false|
|battle-toads|62 12 122123|~?PASSPORT_RF-:6212122123|false|
|battle-toads|72 12 122123|~?PASSPORT_RF-:7212122123|false|
|battle-toads|74 12 122123|~?PASSPORT_RF-:7412122123|false|
|battle-toads|00 12 122123|~?PASSPORT_RF-:0012122123|false|
|battle-toads|52 87 132123|~?PASSPORT_RF-:5287132123|false|
|battle-toads|66 33 976366|~?DRIVER_LICENSE+:6633976366|true|
|battle-toads|00 33 976366|~?DRIVER_LICENSE-:0033976366|false|
|battle-toads|90 33 976366|~?DRIVER_LICENSE-:9033976366|false|
|battle-toads|91 33 976366|~?DRIVER_LICENSE-:9133976366|false|
|battle-toads|92 33 976366|~?DRIVER_LICENSE-:9233976366|false|
|battle-toads|93 33 976366|~?DRIVER_LICENSE-:9333976366|false|
|battle-toads|94 33 976366|~?DRIVER_LICENSE-:9433976366|false|
|battle-toads|95 33 976366|~?DRIVER_LICENSE-:9533976366|false|
|battle-toads|96 33 976366|~?DRIVER_LICENSE-:9633976366|false|
|battle-toads|97 33 976366|~?DRIVER_LICENSE-:9733976366|false|
|battle-toads|98 33 976366|~?DRIVER_LICENSE-:9833976366|false|
|battle-toads|99 33 976366|~?DRIVER_LICENSE+:9933976366|true|
|battle-toads|7o743---777!777|~?INN_UL+:7743777777|false|
|battle-toads|7743жыывав777ывав773|~?INN_UL-:7743777773|false|
|battle-toads|50!0,100732259|~?INN_FL+:500100732259|false|
|battle-toads|65 O9 757573|~?PASSPORT_RF-:6509757573|false|
|battle-toads|65 О9 757573|~?PASSPORT_RF-:6509757573|false|
|battle-toads|66 33 97636O|~?DRIVER_LICENSE+:6633976366|false|
|battle-toads|66 33 97636О|~?DRIVER_LICENSE+:6633976366|false|
|battle-toads|001-001-990-98|== SNILS+:001-001-990-98|false|
|battle-toads|001-001-991-11|== SNILS+:001-001-991-11|false|
|battle-toads|001-001-992-22|== SNILS+:001-001-992-22|false|
|battle-toads|001-001-993-22|== SNILS+:001-001-993-22|false|
|battle-toads|001-001-994-33|== SNILS+:001-001-994-33|false|
|battle-toads|001-001-995-33|== SNILS+:001-001-995-33|false|
|battle-toads|001-001-997-22|== SNILS+:001-001-997-22|false|
|battle-toads|001-001-998-22|== SNILS+:001-001-998-22|false|
|battle-toads|112-233-445-95|== SNILS+:112-233-445-95|true|
|battle-toads|112-233-445-45|== SNILS+:112-233-445-45|false|
|battle-toads|О378ХН26|==GRZ+:О378ХН26|true|
|battle-toads|В163ВС799|==GRZ+:В163ВС799|true|
|battle-toads|К602ВМ097|==GRZ-:К602ВМ097|true|
|battle-toads|у107ор102|==GRZ+:У107ОР102|true|
|battle-toads|0033123350|~?INN_UL-:0033123350|true|
|battle-toads|8834332216|~?INN_UL+:8834332216|true|
|battle-toads|500100732255|~?INN_FL-:500100732255|true|
|battle-toads|5080936111119|~?OGRN+:5080936111119|true|
|battle-toads|50809___3612342_8|~?OGRN+:5080936123428|false|
|battle-toads|50!!809___3612342_2|~?OGRN-:5080936123422|false|
|battle-toads|53!!309,,3612342_5|~?OGRN-:5330936123425|false|
|battle-toads|612741234567899|~?OGRNIP+:612741234567899|true|
|battle-toads|61274!!!123456зы7892|~?OGRNIP-:612741234567892|false|
|battle-toads|612741234557890|~?OGRNIP+:612741234557890|true|
|battle-toads|612741234557893|~?OGRNIP+:612741234557893|false|
|comdiv|252100047973|==INN_FL+|true|
|comdiv|252100047972|==INN_FL-|true|
|comdiv|7707083893|~? INN_UL+|true|
|comdiv|7707083892|~? INN_UL-|true|
|comdiv|6508532353|~? PASSPORT_RF+|true|
|comdiv|6523532353|~? PASSPORT_RF-|false|
|comdiv|A123AA96|~? GRZ+:А123АА96|true|
|comdiv|B123BB96|~? GRZ+:В123ВВ96|true|
|comdiv|E123EE96|~? GRZ+:Е123ЕЕ96|true|
|comdiv|K123KK96|~? GRZ+:К123КК96|true|
|comdiv|M123MM96|~? GRZ+:М123ММ96|true|
|comdiv|H123HH96|~? GRZ+:Н123НН96|true|
|comdiv|O123OO96|~? GRZ+:О123ОО96|true|
|comdiv|P123PP96|~? GRZ+:Р123РР96|true|
|comdiv|C123CC96|~? GRZ+:С123СС96|true|
|comdiv|T123TT96|~? GRZ+:Т123ТТ96|true|
|comdiv|Y123YY96|~? GRZ+:У123УУ96|true|
|comdiv|X123XX96|~? GRZ+:Х123ХХ96|true|
|comdiv|1035006110083|~? OGRN+|true|
|comdiv|1035006110084|~? OGRN-|true|
|comdiv|304500116000157|~? OGRNIP+|true|
|comdiv|304500116000158|~? OGRNIP-|true|
|comdiv|001-001-345 01|==SNILS+|false|
|comdiv|001-001-345 02|==SNILS+|false|
|comdiv|001-001-346 03|==SNILS+|false|
|comdiv|001-002-346 03|==SNILS-|true|
|comdiv|114 765 443 59|==SNILS+|true|
|comdiv|114 765 443 58|==SNILS-|true|
|comdiv|046 860 694 91|==SNILS+|true|
|comdiv|046 860 694 92|==SNILS-|true|
