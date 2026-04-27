import re

path = r'c:\Users\Alberto\Desktop\FlacoFitness\src\main\resources\static\js\tables.js'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

old_line = 'const datasetKey = key.replace(/-([a-z])/g, (_match, letter) => letter.toUpperCase());'
new_lines = 'const fullKey = "ff-" + key;\r\n        const datasetKey = fullKey.replace(/-([a-z])/g, (_match, letter) => letter.toUpperCase());'

content = content.replace(old_line, new_lines)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print('done')
