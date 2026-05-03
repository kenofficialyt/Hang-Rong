# 🏪 HangRong - Minecraft Vendor Stall Plugin

Plugin Minecraft cho phép người chơi tạo sạp hàng di động với NPC, hologram, và hệ thống buôn bán hoàn chỉnh.

**Phiên bản hỗ trợ:** 1.18.x → 1.21.x

## ✨ Tính năng

- 🛒 **Tạo sạp hàng** - Người chơi tự do tạo sạp và buôn bán
- 👤 **NPC tự động** - Spawn NPC khi tạo sạp, click vào để mua hàng
- 📜 **Hologram** - Hiển thị danh sách hàng hóa phía trên NPC
- 💰 **Hệ thống thuế** - Cấu hình % thuế cho mỗi giao dịch
- 🏷️ **Giảm giá theo số lượng** - Mua càng nhiều, giảm càng sâu
- 📊 **Lịch sử giao dịch** - Theo dõi tất cả giao dịch
- 💵 **Hỗ trợ nhiều economy** - Vault & PlayerPoints
- 🎨 **GUI tùy chỉnh** - Chest GUI với bulk purchase
- ⚙️ **Config đầy đủ** - Tùy chỉnh mọi thứ theo ý muốn

## 📋 Yêu cầu

- Spigot/Paper 1.18+
- **Vault** hoặc **PlayerPoints** (ít nhất 1)

## 📥 Cài đặt

### Cách 1: Tải file JAR
1. Tải `HangRong-*.jar` từ [Releases](https://github.com/kenofficialyt/Hang-Rong/releases)
2. Copy vào thư mục `plugins/` của server
3. Khởi động lại server

### Cách 2: Build từ source
```bash
git clone https://github.com/kenofficialyt/Hang-Rong.git
cd Hang-Rong
mvn clean package
```
File jar sẽ nằm ở `target/HangRong-*.jar`

## 🎮 Lệnh

### Người chơi
| Lệnh | Mô tả |
|------|-------|
| `/hr create <tên>` | Tạo sạp hàng mới |
| `/hr delete <tên>` | Xóa sạp hàng của bạn |
| `/hr sell <giá> [số-lượng]` | Bán item đang giữ trên sạp |
| `/hr price <item> <giá>` | Đổi giá mặt hàng |
| `/hr stock <item> <số-lượng>` | Thêm hàng tồn kho |
| `/hr remove <item>` | Xóa item khỏi sạp |
| `/hr list` | Xem danh sách sạp hàng |
| `/hr info <tên>` | Xem thông tin sạp |
| `/hr history [tên]` | Xem lịch sử giao dịch |

### Admin
| Lệnh | Mô tả |
|------|-------|
| `/hr admin reload` | Reload config |
| `/hr admin settax <sạp> <%>` | Đặt thuế tùy chỉnh |
| `/hr admin delete <player>` | Xóa tất cả sạp của player |

## ⚙️ Config

### config.yml
```yaml
economy:
  type: VAULT  # VAULT hoặc PLAYERPOINTS

tax:
  enabled: true
  percentage: 5.0  # % thuế
  tax-account: "server"

npc:
  hologram-enabled: true
  hologram-update-interval: 5
  hologram-display-limit: 5
  click-cooldown: 1.0

gui:
  rows: 4
  title: "&6&lSạp hàng của %seller%"
  fill-empty-slots: true

bulk-pricing:
  enabled: true
  discount-brackets:
    "1": { amount: 8, discount: 5 }
    "2": { amount: 16, discount: 10 }
    "3": { amount: 32, discount: 15 }
    "4": { amount: 64, discount: 20 }

vendor:
  max-items-per-stall: 27
  max-stalls-per-player: 3
  auto-save-interval: 300
```

## 🔑 Permissions

| Permission | Mô tả | Default |
|------------|-------|---------|
| `hangrong.use` | Quyền sử dụng cơ bản | true |
| `hangrong.create` | Tạo sạp hàng | true |
| `hangrong.delete` | Xóa sạp hàng | true |
| `hangrong.sell` | Bán hàng trên sạp | true |
| `hangrong.admin` | Quyền admin | op |
| `hangrong.admin.reload` | Reload config | op |
| `hangrong.admin.others` | Xem sạp người khác | op |
| `hangrong.admin.delete` | Xóa sạp bất kỳ | op |
| `hangrong.admin.settax` | Đặt thuế tùy chỉnh | op |
| `hangrong.admin.bypass-tax` | Miễn thuế | op |
| `hangrong.max-stalls.<số>` | Giới hạn sạp hàng | 3 |

## 🤝 Đóng góp

Mọi đóng góp đều được chào đón! Hãy tạo Issue hoặc Pull Request.

## 📄 License

MIT License

---

Made with ❤️ by [kenofficialyt](https://github.com/kenofficialyt)
