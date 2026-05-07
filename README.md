# 🏪 HangRong - Plugin Sạp Hàng Rong Minecraft

Plugin Minecraft cho phép người chơi tạo sạp hàng rong di động, mua bán vật phẩm với NPC người bán.

## ✨ Tính năng

- **NPC Người Bán**: Armor Stand với skin người bán hàng (mũ, áo nâu, giày)
- **GUI Mua Bán**: Giao diện đẹp mắt với bulk pricing giảm giá theo số lượng
- **Hologram**: Hiển thị thông tin sạp hàng phía trên NPC (tối ưu, không trùng lặp)
- **Hệ thống thuế**: Thuế tự động trừ khi có permission bypass
- **Lịch sử giao dịch**: Lưu và xem lại lịch sử mua bán
- **Multiple Stalls**: Mỗi người chơi có thể tạo nhiều sạp hàng

## 📋 Yêu cầu

- Minecraft Server 1.20+
- Java 17+
- [Vault](https://www.spigotmc.org/resources/vault.34315/) (cho economy)

## 🚀 Cài đặt

1. Tải file `HangRong-1.0.4.jar` từ [Releases](https://github.com/kenofficialyt/Hang-Rong/releases)
2. Copy vào thư mục `plugins/` của server
3. Khởi động lại server
4. Cấu hình trong `plugins/HangRong/config.yml`

## 🎮 Cách dùng

### Người chơi
```
/hr create <tên>                          # Tạo sạp hàng mới
/hr delete <tên>                          # Xóa sạp hàng của mình
/hr list                                  # Xem danh sách sạp hàng
/hr sell <tên> <giá> <số lượng>           # Bán vật phẩm đang cầm
/hr price <tên> <mục> <giá mới>           # Đổi giá mặt hàng
/hr stock <tên> <mục> <số lượng mới>      # Đổi số lượng tồn kho
/hr remove <tên> <mục>                    # Xóa mặt hàng khỏi sạp
/hr info <tên>                            # Xem thông tin sạp hàng
/hr history [tên]                         # Xem lịch sử giao dịch
/hr reload                                # Reload config (admin)
```

### Admin
```
/hr admin reload                          # Reload plugin
/hr admin settax <tên sạp> <% thuế>       # Đặt thuế tùy chỉnh cho sạp
/hr admin delete <tên người chơi>         # Xóa tất cả sạp của người chơi
```

### Mua hàng
- **Click vào NPC** người bán để mở GUI
- Chọn vật phẩm → chọn số lượng → xác nhận mua

## ⚙️ Cấu hình

### config.yml
```yaml
economy:
  type: VAULT          # VAULT hoặc PLAYERPOINTS

tax:
  enabled: true
  percentage: 5.0      # Thuế %
  bypass-permission: "hangrong.admin.bypass-tax"

npc:
  hologram-enabled: true
  hologram-update-interval: 5   # Giây
  hologram-line-height: 0.4
  hologram-display-limit: 3     # Số dòng hiển thị
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
  auto-save-interval: 300  # Giây
```

## 📦 Permissions

### Người chơi
| Permission | Mô tả |
|---|---|
| `hangrong.use` | Sử dụng plugin cơ bản |
| `hangrong.create` | Tạo sạp hàng |
| `hangrong.delete` | Xóa sạp hàng của mình |
| `hangrong.sell` | Bán vật phẩm trên sạp |

### Admin
| Permission | Mô tả |
|---|---|
| `hangrong.admin` | Truy cập lệnh admin |
| `hangrong.admin.reload` | Reload plugin |
| `hangrong.admin.others` | Xem/sửa sạp hàng người khác |
| `hangrong.admin.delete` | Xóa sạp hàng bất kỳ |
| `hangrong.admin.settax` | Đặt thuế tùy chỉnh |
| `hangrong.admin.bypass-tax` | Miễn thuế khi mua |

## 🔄 Changelog

### v1.0.4
- ✅ Fix lỗi không mua được đồ (regex bulk select)
- ✅ Ngăn drag đồ trong GUI
- ✅ Giảm hologram trùng lặp, update in-place
- ✅ NPC skin người bán (áo nâu, giày đen, mũ player head)
- ✅ Giảm hologram lines: 5 → 3

## 📝 License

[MIT License](LICENSE)

## 👤 Author

**kenofficialyt** - [GitHub](https://github.com/kenofficialyt)
