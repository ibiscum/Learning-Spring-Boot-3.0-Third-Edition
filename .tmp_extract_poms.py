#!/usr/bin/env python3
import json, os, re, sys
from xml.etree import ElementTree as ET
from collections import defaultdict

root = '/home/ulf/daten/Learning-Spring-Boot-3.0-Third-Edition'
find = []
for dirpath, dirnames, filenames in os.walk(root):
    for f in filenames:
        if f == 'pom.xml':
            find.append(os.path.join(dirpath, f))
find.sort()

# Helper: resolve ${...}
def resolve_property(value, properties):
    if value is None:
        return None
    pattern = re.compile(r"\$\{([^}]+)\}")
    changed = True
    while changed:
        changed = False
        def repl(match):
            name = match.group(1)
            if name in properties:
                nonlocal changed
                changed = True
                return properties[name]
            return match.group(0)
        value2 = pattern.sub(repl, value)
        if value2 != value:
            changed = True
            value = value2
    return value

results = []
for pom in find:
    with open(pom, 'r', encoding='utf-8') as f:
        data = f.read()
    # parse properties and dependencyManagement from XML
    try:
        tree = ET.fromstring(data)
    except ET.ParseError as e:
        # fallback by removing BOM or trying recover style? not needed
        continue
    ns = {'m': tree.tag[1:].split('}')[0]} if tree.tag.startswith('{') else {}
    def findall(elem, path):
        if ns:
            return elem.findall(path, ns)
        return elem.findall(path)
    props = {}
    # get properties
    for prop in findall(tree, './/m:properties' if ns else './/properties'):
        for child in list(prop):
            tag = child.tag
            if tag.startswith('{'):
                tag = tag.split('}',1)[1]
            props[tag] = child.text.strip() if child.text else ''
    # parse dependencyManagement versions
    depm_versions = {}
    depm = findall(tree, './/m:dependencyManagement' if ns else './/dependencyManagement')
    for dm in depm:
        for dep in findall(dm, './/m:dependency' if ns else './/dependency'):
            g = findall(dep, 'm:groupId' if ns else 'groupId')
            a = findall(dep, 'm:artifactId' if ns else 'artifactId')
            v = findall(dep, 'm:version' if ns else 'version')
            if g and a and v and g[0].text and a[0].text and v[0].text:
                depm_versions[(g[0].text.strip(), a[0].text.strip())] = v[0].text.strip()
    # parse dependencies with line numbers via simple scan
    lines = data.splitlines()
    dep_line_nums = []
    for idx, line in enumerate(lines, start=1):
        if '<dependency' in line and not line.strip().startswith('<!--'):
            dep_line_nums.append(idx)
    # parse direct dependencies in pom
    for dep in findall(tree, './/m:dependencies/m:dependency' if ns else './/dependencies/dependency'):
        g = findall(dep, 'm:groupId' if ns else 'groupId')
        a = findall(dep, 'm:artifactId' if ns else 'artifactId')
        v = findall(dep, 'm:version' if ns else 'version')
        if not g or not a:
            continue
        groupId = g[0].text.strip() if g[0].text else ''
        artifactId = a[0].text.strip() if a[0].text else ''
        version = v[0].text.strip() if v else None
        if version:
            version = resolve_property(version, props)
        else:
            version = depm_versions.get((groupId, artifactId))
            if version:
                version = resolve_property(version, props)
        if version and '${' in version:
            version = resolve_property(version, props)
        if not version or '${' in version:
            continue
        # try to assign line number from dep section order
        if dep_line_nums:
            lineNumber = dep_line_nums.pop(0)
        else:
            lineNumber = 1
        results.append({
            'filePath': pom,
            'lineNumber': lineNumber,
            'gav': f'{groupId}:{artifactId}:{version}'
        })

print(json.dumps(results, indent=2))
