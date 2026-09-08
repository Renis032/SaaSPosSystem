import { jsPDF } from 'jspdf'
import type { Receipt } from '@/types/models'
import type { ActiveBranch } from '@/lib/branch-store'

function money(value?: number) {
  return `$${(value ?? 0).toFixed(2)}`
}

export function downloadReceiptPdf(
  receipt: Receipt,
  options?: {
    branch?: ActiveBranch | null
    paymentType?: string
    tendered?: number
    change?: number
  },
) {
  const doc = new jsPDF({ unit: 'pt', format: 'letter' })
  const left = 48
  let y = 56

  doc.setFont('helvetica', 'bold')
  doc.setFontSize(18)
  doc.text(receipt.storeName || 'Renko POS', left, y)
  y += 22

  doc.setFont('helvetica', 'normal')
  doc.setFontSize(11)
  if (receipt.storeAddress) {
    doc.text(receipt.storeAddress, left, y)
    y += 16
  }
  if (receipt.storePhone) {
    doc.text(receipt.storePhone, left, y)
    y += 16
  }
  if (options?.branch) {
    doc.text(`Branch: ${options.branch.name}`, left, y)
    y += 16
  }

  y += 8
  doc.setFont('helvetica', 'bold')
  doc.text(`Receipt ${receipt.receiptNumber ?? `#${receipt.orderId}`}`, left, y)
  y += 18
  doc.setFont('helvetica', 'normal')
  doc.text(
    `Date: ${receipt.orderDate ? new Date(receipt.orderDate).toLocaleString() : '—'}`,
    left,
    y,
  )
  y += 16
  doc.text(`Cashier: ${receipt.cashierName ?? '—'}`, left, y)
  y += 16
  if (options?.paymentType) {
    doc.text(`Payment: ${options.paymentType}`, left, y)
    y += 16
  }

  y += 10
  doc.setDrawColor(180)
  doc.line(left, y, 560, y)
  y += 22

  for (const item of receipt.items ?? []) {
    const label = `${item.quantity} × ${item.name}`
    const amount = money(item.lineTotal ?? item.finalPrice)
    doc.text(label, left, y)
    doc.text(amount, 560, y, { align: 'right' })
    y += 18
    if (y > 720) {
      doc.addPage()
      y = 56
    }
  }

  y += 8
  doc.line(left, y, 560, y)
  y += 22

  const total = (receipt.items ?? []).reduce(
    (sum, item) => sum + (item.lineTotal ?? item.finalPrice ?? 0),
    0,
  )
  doc.setFont('helvetica', 'bold')
  doc.text('Total', left, y)
  doc.text(money(total), 560, y, { align: 'right' })
  y += 18

  if (options?.tendered != null) {
    doc.setFont('helvetica', 'normal')
    doc.text('Tendered', left, y)
    doc.text(money(options.tendered), 560, y, { align: 'right' })
    y += 16
    doc.text('Change', left, y)
    doc.text(money(options.change ?? 0), 560, y, { align: 'right' })
  }

  y += 28
  doc.setFontSize(10)
  doc.setTextColor(100)
  doc.text('Thank you for shopping with Renko.', left, y)

  const filename = `renko-receipt-${receipt.receiptNumber ?? receipt.orderId}.pdf`
  doc.save(filename)
}
