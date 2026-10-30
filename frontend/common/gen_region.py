import json

SRC = r"D:\IdeaProject\study-checkin-frontend\common\area-raw.json"
DST = r"D:\IdeaProject\study-checkin-frontend\common\region.js"

with open(SRC, encoding="utf-8") as f:
    data = json.load(f)

provinces = data.get("86", {})
result = []

for pcode, pname in provinces.items():
    sub = data.get(pcode)
    if not sub:
        continue
    cities = []
    direct_districts = []  # 省直辖县级单位（无市级）
    for ccode, cname in sub.items():
        districts = data.get(ccode)
        if districts:
            # 正常地级市/直辖市"市辖区"
            city_name = pname if cname == "市辖区" else cname
            cities.append({"name": city_name, "districts": list(districts.values())})
        else:
            # 省直辖县级单位（如济源市、省直辖县），并到"省直辖"下
            direct_districts.append(cname)
    if direct_districts:
        cities.append({"name": pname, "districts": direct_districts})
    if cities:
        result.append({"name": pname, "cities": cities})

# 生成 region.js（ES module）
lines = []
lines.append("// 完整省市区数据（数据来源：airyland/china-area-data，覆盖全国）")
lines.append("// 结构：province.name / province.cities[].name / city.districts[]")
lines.append("const regionData = ")
lines.append(json.dumps(result, ensure_ascii=False, indent=2))
lines.append("")
lines.append("export default regionData")
lines.append("")

with open(DST, "w", encoding="utf-8") as f:
    f.write("\n".join(lines))

print("provinces:", len(result))
total_cities = sum(len(p["cities"]) for p in result)
total_districts = sum(len(c["districts"]) for p in result for c in p["cities"])
print("cities:", total_cities, "districts:", total_districts)
